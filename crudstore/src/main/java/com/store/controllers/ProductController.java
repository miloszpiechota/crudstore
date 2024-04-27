package com.store.controllers;

import com.store.models.Product;
import com.store.models.ProductDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Poprawny import
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.store.services.ProductRepository;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Binding;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping({"","/"})
    public String showProductList(Model model){
        List<Product> products = repo.findAll();
        model.addAttribute("products",products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreateProduct(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto",productDto);
        return "products/CreateProduct";
    }

    @PostMapping("/create")
    public String createProduct(@Valid @ModelAttribute ProductDto productDto,
                                BindingResult result) {


//    if(productDto.getImageFile().isEmpty()){
//        result.addError(new FieldError("productDto","imageFile","The image file is required"));
//    }

    if(result.hasErrors()){
        return "products/CreateProduct";
    }

    //saving image
    MultipartFile image = productDto.getImageFile();
    //create unique file name
    Date createdAt = new Date();
    String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
    try {
        String uploadDir = "public/images/";
        Path uploadPath = Paths.get(uploadDir);

        if(!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }

        try(InputStream inputStream = image.getInputStream()) {
            Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                    StandardCopyOption.REPLACE_EXISTING);

        }
    } catch (Exception e){
        System.out.println("Exception: " + e.getMessage());
    }

    //adding new product
        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);

        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id){
        try {
            // Retrieve the product from the repository
            Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));

            // Add the product to the model with the correct attribute name
            model.addAttribute("product", product);

            // Create a new ProductDto and set its values from the product
            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());

            // Add the productDto to the model
            model.addAttribute("productDto", productDto);
        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/products";
        }
        return "products/EditProduct";
    }


    @PostMapping("/edit")
    public String updateProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result) {

        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "products/EditProduct";
            }

            if (!productDto.getImageFile().isEmpty()) {
                // Delete old image
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
                try {
                    Files.delete(oldImagePath);
                } catch (IOException e) {
                    System.out.println("Exception: " + e.getMessage());
                }

                // Save new image file
                MultipartFile image = productDto.getImageFile();
                // Create unique file name
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(storageFileName);
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());
            repo.save(product);

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id){
        try{
            // Retrieve the product from the repository
            Product product = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));

            // Delete the product image
            Path imagePath = Paths.get("public/images/" + product.getImageFileName());
            try{
                Files.delete(imagePath);
            } catch (Exception e){
                System.out.println("Exception deleting image: " + e.getMessage());
                // Optionally handle the exception here (e.g., log the error)
            }

            // Delete the product from the database
            repo.delete(product);
        } catch (Exception e){
            System.out.println("Exception deleting product: " + e.getMessage());
            // Optionally handle the exception here (e.g., log the error)
        }
        return("redirect:/products");
    }




}
