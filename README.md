## jspecify-demo
This project demonstrate on how to utilize jspecify library to avoid NPE (NullPointerException) in java application with the help of Google ErrorProne and Uber NullAway checkers.

### Dependency Setup
- As I'm dealing with SpringBoot <sup>4</sup>, jspecify library is part of spring dependency. If you are dealing with lower version of spring boot or any java project add below dependency to your build.gradle file.

```
// gradle dependency
implementation 'org.jspecify:jspecify:1.0.0

//Maven dependency
<dependency>
  <groupId>org.jspecify</groupId>
  <artifactId>jspecify</artifactId>
  <version>1.0.0</version>
</dependency>
        
```

### Implement the code to avoid NullPointerExceptions using jspecify annotations (@Nullable/@NonNull for method parameters/returns)

- Package level restriction
  Create package-info.java file under a package which you are trying to include as part of checker validation then add @NullMarked annotation as below

```
@NullMarked
package com.shyam.jspecify.controller;

import org.jspecify.annotations.NullMarked;

```
- Class level restriction
  Add @NullMarked annotation as class level so that it applies to all methods in a class

```
@Service
@NullMarked
public class OrderService {
    public @Nullable String getOrder(@NonNull String id, @Nullable Integer page) {
        if(page > 10) {
            return "Invalid Order Id";
        }
        return StringUtils.isNumeric(id) ? null : "Order details for id: " + id;
    }
}

```

- Method level restriction
  Add @NullMarked annotation as method level so that it applies to that specific method in a class

```
    @NullMarked
    public @Nullable String getOrder(@NonNull String id, @Nullable Integer page) {
        if(page > 10) {
            return "Invalid Order Id";
        }
        return StringUtils.isNumeric(id) ? null : "Order details for id: " + id;
    }

```

If you compile the code, though you applied the annotations on parameters/returns, IDE will show you the warning messages but compile will be happy and build is successful.

```
xxxxx@XXXXPro jspecify-demo % ./gradlew clean build
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended

BUILD SUCCESSFUL in 6s
8 actionable tasks: 8 executed

```

In order to break the build whenever there are potential NPEs, we need to configure a checker framework which constantly looks for NPEs in the code breaks the build whenever if it detects such instances. 

### Checker Setup

- Add ErrorProne plugin as below

``` 
plugins {
        id 'java'
        id 'org.springframework.boot' version '4.0.0-SNAPSHOT'
        id 'io.spring.dependency-management' version '1.1.7'
        id "net.ltgt.errorprone" version "4.0.0" //Error Prone plugin
    }
```

- Add ErrorProne, NullAway dependencies to build.gradle as below.

```
dependencies {
	.........
	.........
	.........
	errorprone "com.google.errorprone:error_prone_core:2.41.0"
	errorprone "com.uber.nullaway:nullaway:0.12.8"
}
```

- Setup compile to consider ErrorProne/NullAway as checkers during project build in build.gradle file

Use either one of the option from below for checker but not both
**NullAway:AnnotatedPackages** OR **NullAway:OnlyNullMarked** 

```
import net.ltgt.gradle.errorprone.CheckSeverity

tasks.withType(JavaCompile) {
	options.compilerArgs += ["--should-stop=ifError=FLOW" ]
	options.errorprone {
		check("NullAway", CheckSeverity.ERROR)
		//  You'll also need to tell NullAway which packages to check. For example:
		//option("NullAway:AnnotatedPackages", "com.shyam.jspecify")
		
		// You can specify to consider packages market with @NullAware annotation
		option("NullAway:OnlyNullMarked", "true")
		
	}
}
```

#### How the compiler behave when we apply checker and build the project?

```
xxxxxx@xxxxxxPro jspecify-demo % ./gradlew clean build

> Task :compileJava FAILED
/Users/xxxxxx/Workspace/java_projects/jspecify-demo/src/main/java/com/shyam/jspecify/controller/OrderController.java:16: error: [NullAway] returning @Nullable expression from method with @NonNull return type
        return orderService.getOrder(id, page);
        ^
    (see http://t.uber.com/nullaway )
/Users/xxxxxx/Workspace/java_projects/jspecify-demo/src/main/java/com/shyam/jspecify/service/OrderService.java:13: error: [NullAway] unboxing of a @Nullable value
        if(page > 10) {
           ^
    (see http://t.uber.com/nullaway )
2 errors

[Incubating] Problems report is available at: file:///Users/xxxxxx/Workspace/java_projects/jspecify-demo/build/reports/problems/problems-report.html

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':compileJava'.
> Compilation failed; see the compiler output below.
  /Users/xxxxxx/Workspace/java_projects/jspecify-demo/src/main/java/com/shyam/jspecify/controller/OrderController.java:16: error: [NullAway] returning @Nullable expression from method with @NonNull return type
          return orderService.getOrder(id, page);
          ^
      (see http://t.uber.com/nullaway )
  2 errors

* Try:
> Check your code and dependencies to fix the compilation error(s)
> Run with --scan to get full insights.

BUILD FAILED in 2s
2 actionable tasks: 2 executed


```


### Maven Checker Setup

- Add ErrorProne plugin, dependencies, compiler setup

Use either one of the option from below for checker but not both
**NullAway:AnnotatedPackages** OR **NullAway:OnlyNullMarked**

``` 
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
                    <arg>-XDcompilePolicy=simple</arg> <!-- Compiler policy 'simple' help to run checker-->
                    <arg>--should-stop=ifError=FLOW</arg> <!-- Stops the compiler flow if there is any error reported -->
                    <arg>
                        -Xplugin:ErrorProne <!-- Ask the compiler to use ErrorProne plugin-->
                        -Xep:NullAway:ERROR <!-- Tells the ErrorProne plugin to consider NullAway Errors -->
                        -XepOpt:NullAway:AnnotatedPackages=com.shyam.jspecify <!-- Tells the ErrorProne plugin to consider packages listed here-->
                        <!-- -XepOpt:NullAway:OnlyNullMarked=true --> <!-- Tells the ErrorProne plugin to consider packages annotated with @NullMarked-->
                    </arg>
                </compilerArgs>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </path>
                    <path>
                        <groupId>com.google.errorprone</groupId>
                        <artifactId>error_prone_core</artifactId>
                        <version>2.41.0</version>
                    </path>
                    <path>
                        <groupId>com.uber.nullaway</groupId>
                        <artifactId>nullaway</artifactId>
                        <version>0.12.8</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        ........... <!-- other plugins -->
    </plugins>
    </build>
```

#### How the compiler behave when we apply checker and build the project?

```
XXXXX@XXXXXPro jspecify-demo-mvn % ./mvnw clean compile
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------< com.XXXXX.jspecify:jspecify-demo-mvn >----------------
[INFO] Building jspecify-demo-mvn 0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.5.0:clean (default-clean) @ jspecify-demo-mvn ---
[INFO] Deleting /Users/XXXXX/Workspace/java_projects/jspecify-demo-mvn/target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ jspecify-demo-mvn ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] Copying 0 resource from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.14.0:compile (default-compile) @ jspecify-demo-mvn ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 5 source files with javac [debug parameters release 21] to target/classes
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] /Users/XXXXX/Workspace/java_projects/jspecify-demo-mvn/src/main/java/com/shyam/jspecify/controller/OrderController.java:[16,9] [NullAway] returning @Nullable expression from method with @NonNull return type
    (see http://t.uber.com/nullaway )
[ERROR] /Users/XXXXX/Workspace/java_projects/jspecify-demo-mvn/src/main/java/com/shyam/jspecify/service/OrderService.java:[11,12] [NullAway] unboxing of a @Nullable value
    (see http://t.uber.com/nullaway )
[INFO] 2 errors 
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.590 s
[INFO] Finished at: 2025-08-19T14:45:54-07:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.14.0:compile (default-compile) on project jspecify-demo-mvn: Compilation failure: Compilation failure: 
[ERROR] /Users/XXXXX/Workspace/java_projects/jspecify-demo-mvn/src/main/java/com/shyam/jspecify/controller/OrderController.java:[16,9] [NullAway] returning @Nullable expression from method with @NonNull return type
[ERROR]     (see http://t.uber.com/nullaway )
[ERROR] /Users/XXXXX/Workspace/java_projects/jspecify-demo-mvn/src/main/java/com/shyam/jspecify/service/OrderService.java:[11,12] [NullAway] unboxing of a @Nullable value
[ERROR]     (see http://t.uber.com/nullaway )
[ERROR] -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException

```

### References

[JSpecify](https://jspecify.dev/docs/start-here/)

[Google Error Prone](https://github.com/google/error-prone)

[Error Prone Docs](https://errorprone.info/docs/installation)

[NullAway](https://github.com/uber/NullAway)

[NullAway Configurations](https://github.com/uber/NullAway/wiki/Configuration)