package com.example.qmanagement.controller;

import com.example.qmanagement.dto.PurchaseRequest;
import com.example.qmanagement.entity.Product;
import com.example.qmanagement.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user/products")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping("/purchase")
    @Transactional
    public ResponseEntity<?> purchaseProduct(@RequestBody PurchaseRequest request) {
        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStock() >= request.getQuantity()) {
                product.setStock(product.getStock() - request.getQuantity());
                productRepository.save(product);
                return ResponseEntity.ok("购买成功，库存已更新。");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("库存不足，无法完成购买。");
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("商品不存在。");
    }
}
