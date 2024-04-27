package com.store.controllers;

import com.store.models.Product;
import com.store.models.ProductDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Poprawny import
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.store.services.ProductRepository;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping({"","/"})
    public String showProductList(Model model){ // Poprawiony import
        List<Product> products = repo.findAll();
        model.addAttribute("products",products);
        return "products/index";
    }

    @GetMapping({"/create"})
    public String showCreateProduct(Model model){ // Poprawiony import
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto",productDto);
        return "products/CreateProduct";
    }



}
