package com.example.admissions_management.presentation.web.controller;

import com.example.admissions_management.application.dto.request.UserAccountRequest;
import com.example.admissions_management.application.dto.response.UserAccountResponse;
import com.example.admissions_management.application.service.UserAccountService;
import com.example.admissions_management.domain.model.UserRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users") // Chỉ sử dụng một tiền tố duy nhất
public class UserManagementController {

    private final UserAccountService userAccountService;

    public UserManagementController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userAccountService.getAllUsers());
        return "users";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        if (id == 0) {
            UserAccountRequest request = new UserAccountRequest();
            request.setEnabled(true);
            request.setRole(UserRole.USER);
            model.addAttribute("userForm", request);
            return "user-form";
        }

        UserAccountResponse user = userAccountService.getUserById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
            return "redirect:/admin/users"; // Redirect về /admin/users
        }
        
        UserAccountRequest request = new UserAccountRequest();
        request.setId(user.getId());
        request.setUsername(user.getUsername());
        request.setFullName(user.getFullName());
        request.setRole(user.getRole());
        request.setEnabled(user.getEnabled());
        model.addAttribute("userForm", request);
        return "user-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("userForm") UserAccountRequest request, BindingResult result, RedirectAttributes redirectAttributes) {
        // Validation logic
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            result.rejectValue("username", "username.empty", "Username không được để trống.");
        }
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            result.rejectValue("fullName", "fullName.empty", "Họ tên không được để trống.");
        }
        if (request.getId() == null && (request.getPassword() == null || request.getPassword().isBlank())) {
            result.rejectValue("password", "password.empty", "Mật khẩu bắt buộc khi tạo tài khoản mới.");
        }

        if (result.hasErrors()) {
            // Khi có lỗi, nên trả về trang form thay vì redirect để hiện lỗi validation
            return "user-form"; 
        }

        userAccountService.save(request);
        redirectAttributes.addFlashAttribute("successMessage", "Đã lưu người dùng thành công.");
        return "redirect:/admin/users"; // Luôn redirect về /admin/users
    }

    @PostMapping("/change-password/{id}")
    public String changePassword(@PathVariable("id") Long id, UserAccountRequest request, RedirectAttributes redirectAttributes) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới không được để trống.");
        } else {
            userAccountService.changePassword(id, request.getPassword());
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công.");
        }
        return "redirect:/admin/users/edit/" + id; // Giữ đúng tiền tố /admin
    }

    @PostMapping("/toggle-role/{id}")
    public String toggleRole(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        UserAccountResponse user = userAccountService.getUserById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
        } else {
            userAccountService.setRole(id, user.getRole() == UserRole.ADMIN ? UserRole.USER : UserRole.ADMIN);
            redirectAttributes.addFlashAttribute("successMessage", "Đã đổi quyền thành công.");
        }
        return "redirect:/admin/users"; // Redirect về /admin/users
    }

    @PostMapping("/set-enabled/{id}")
    public String setEnabled(@PathVariable("id") Long id, @RequestParam(value = "enabled", required = false) Boolean enabled, RedirectAttributes redirectAttributes) {
        userAccountService.setEnabled(id, enabled != null && enabled);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công.");
        return "redirect:/admin/users"; // Redirect về /admin/users
    }

    @ModelAttribute("roleOptions")
    public UserRole[] roleOptions() {
        return UserRole.values();
    }
}