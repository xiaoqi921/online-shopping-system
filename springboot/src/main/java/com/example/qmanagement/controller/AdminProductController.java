package com.example.qmanagement.controller;

import com.example.qmanagement.entity.Product;
import com.example.qmanagement.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*")
public class AdminProductController {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 获取所有商品，支持按名称和分类搜索
     */
    @GetMapping
    public List<Product> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        if ((name == null || name.isBlank()) && (category == null || category.isBlank())) {
            return productRepository.findAll();
        }
        String nameParam = (name == null || name.isBlank()) ? null : name.trim();
        String categoryParam = (category == null || category.isBlank()) ? null : category.trim();
        return productRepository.searchProducts(nameParam, categoryParam);
    }

    /**
     * 获取单个商品
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 新增商品（含表单校验）
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        Map<String, String> errors = validateProduct(product);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            product.setCategory("其他");
        }
        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    /**
     * 修改商品
     */
    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Map<String, String> errors = validateProduct(productDetails);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStock(productDetails.getStock());
            product.setImageUrl(productDetails.getImageUrl());
            product.setCategory(
                productDetails.getCategory() == null || productDetails.getCategory().isBlank()
                    ? "其他" : productDetails.getCategory()
            );
            return ResponseEntity.ok(productRepository.save(product));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id:[0-9]+}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 获取所有分类列表
     */
    @GetMapping("/categories")
    public List<String> getCategories() {
        return productRepository.findAll().stream()
                .map(Product::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    // ---- 私有校验方法 ----
    private Map<String, String> validateProduct(Product p) {
        Map<String, String> errors = new HashMap<>();
        if (p.getName() == null || p.getName().isBlank()) {
            errors.put("name", "商品名称不能为空");
        }
        if (p.getPrice() == null || p.getPrice().doubleValue() < 0) {
            errors.put("price", "价格不能为负数");
        }
        if (p.getStock() == null || p.getStock() < 0) {
            errors.put("stock", "库存不能为负数");
        }
        return errors;
    }
}
