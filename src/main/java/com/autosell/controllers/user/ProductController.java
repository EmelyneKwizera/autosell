package com.autosell.controllers.user;

import com.autosell.domains.Product;
import com.autosell.helpers.MyHelper;
import com.autosell.services.CategoryService;
import com.autosell.services.FilesStorageService;
import com.autosell.services.ProductService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/account/product-list")
public class ProductController {
    @Autowired
    private MessageSource messageSource;

    @Autowired
    WebApplicationContext servletContext;

    @Autowired
    ProductService productService;


    @Autowired
    CategoryService categoryService;

    @GetMapping(value = {"", "/"})
    public String index(Model model) {
        model.addAttribute("products", productService.findAll());
        return "user/product_list";
    }

    @GetMapping("/add")
    public String showProductForm(@ModelAttribute("product") Product product, Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "user/product_form";
    }

    @PostMapping("/add")
    public String addProduct(@RequestParam("product_file") MultipartFile product_file, @Valid @ModelAttribute("product") Product product, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, HttpServletRequest httpRequest, Locale loc) {

        if (product_file == null) {

            String localeMsg = messageSource.getMessage("productPicRequired", null, null, loc);
            bindingResult.addError(new FieldError("product", "product_image", "", false, null, null, localeMsg));
        } else if (product_file.getSize() == 0) {
            String localeMsg = messageSource.getMessage("productPicRequired", null, null, loc);
            bindingResult.addError(new FieldError("product", "product_image", "", false, null, null, localeMsg));
        } else {
            String rootDirectory = httpRequest.getServletContext().getRealPath("/");
            try {
                String productName = MyHelper.getRandomInt() + "." + FilenameUtils.getExtension(product_file.getOriginalFilename());

                File file = new File(rootDirectory + "/images/");
                file.mkdirs();
                System.out.println("rootDirectoryrootDirectory");
                System.out.println(rootDirectory);
                product_file.transferTo(new File(rootDirectory + "/images/" + productName));
                product.setProductImage(productName);
            } catch (Exception e) {

                String localeMsg = messageSource.getMessage("unableToUpload", null, null, loc);
                bindingResult.addError(new FieldError("product", "product_image", "", false, null, null, localeMsg));
            }

        }
        if (bindingResult.hasErrors()) {

            model.addAttribute("categories", categoryService.findAll());
            return "user/product_form";
        } else {
            productService.save(product);
            redirectAttributes.addFlashAttribute("success_msg", "Product has been created successfully.");
            return "redirect:/account/product-list";
        }


    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String showEditForm(@PathVariable("id") long id, Model model) {

        Optional<Product> employeeOptional = productService.findById(id);
        model.addAttribute("product", employeeOptional.get());

        model.addAttribute("categories", categoryService.findAll());
        return "user/product_form";
    }

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.POST)
    public String updateEmployee(@RequestParam("product_file") MultipartFile product_file,@Valid @ModelAttribute("product") Product product, BindingResult bindingResult, @PathVariable("id") long id,RedirectAttributes redirectAttributes, Model model,HttpServletRequest httpRequest,Locale loc) {
        if (product_file != null & product_file.getSize()>0){
            String rootDirectory = httpRequest.getServletContext().getRealPath("/");
            try {
                String productName = MyHelper.getRandomInt() + "." + FilenameUtils.getExtension(product_file.getOriginalFilename());

                File file = new File(rootDirectory + "/images/");
                file.mkdirs();
                System.out.println("rootDirectoryrootDirectory");
                System.out.println(rootDirectory);
                product_file.transferTo(new File(rootDirectory + "/images/" + productName));
                product.setProductImage(productName);
            } catch (Exception e) {

                String localeMsg = messageSource.getMessage("unableToUpload", null, null, loc);
                bindingResult.addError(new FieldError("product", "product_image", "", false, null, null, localeMsg));
            }

        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "user/product_form";
        }else{
            redirectAttributes.addFlashAttribute("success_msg", "Product has been created successfully.");
            product.setId(id);
            productService.save(product);

            return ("redirect:/account/product-list");
        }

    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public String deleteEmployee(@PathVariable("id") long id, Model model,RedirectAttributes redirectAttributes) {
        Optional<Product> employeeOptional = productService.findById(id);
        if(employeeOptional.isPresent()){
            Product product = employeeOptional.get();
            if(product.isSoldStatus() == true){
                redirectAttributes.addFlashAttribute("error_msg", "This product has been ordered by customer.");
            }else{
                productService.deleteById(id);
                redirectAttributes.addFlashAttribute("success_msg", "Product has been deleted successfully.");
            }

        }else{
            redirectAttributes.addFlashAttribute("error_msg", "Product not found.");
        }

        //pending to check order table
        return ("redirect:/account/product-list");
    }

}
