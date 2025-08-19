package com.shyam.jspecify.controller;

import com.shyam.jspecify.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class OrderController {
    private OrderService orderService;

    @GetMapping("/{id}")
    public String getOrder(@RequestParam String id, @PathVariable("page") Integer page){
        return orderService.getOrder(id, page);
    }
}
