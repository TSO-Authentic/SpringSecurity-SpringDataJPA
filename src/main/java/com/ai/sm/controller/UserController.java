package com.ai.sm.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ai.sm.model.SearchBean;
import com.ai.sm.model.UserBean;
import com.ai.sm.persistant.dto.UserDTO;
import com.ai.sm.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService uService;

	@ModelAttribute("currentUserName")
	public String  getCurrentUsername(HttpServletRequest session) {

		String currentUserId = session.getRemoteUser();
		List<UserDTO> currentUser = new ArrayList<>();
		currentUser = uService.findByIdOrName(currentUserId, "");
		String currentUserName = currentUser.get(0).getName();
		return currentUserName;
	}

	@GetMapping(value = "/welcome")
	public String mainPage() {
		return "M00001";
	}

	@GetMapping(value = "/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}

	@GetMapping("/setupUser")
	public ModelAndView setupUser(@ModelAttribute("Error") String Error, @ModelAttribute("Success") String Success,
			ModelMap model) {
		model.addAttribute("Error", Error);
		model.addAttribute("Success", Success);
		
		
		return new ModelAndView("USR001", "sBean", new SearchBean());
	}

	@GetMapping(value = "/searchUser")
	public String searchUser(@ModelAttribute("sBean") SearchBean searchBean, ModelMap model) {

		List<UserDTO> userList = new ArrayList<>();
		UserDTO uDTO = new UserDTO();

		uDTO.setId(searchBean.getUserId());
		uDTO.setName(searchBean.getUserName());

		if (!uDTO.getId().equals("") || !uDTO.getName().equals("")) {
			userList = uService.findByIdOrName(uDTO.getId(), uDTO.getName());
		} else {
			userList = uService.findAll();
		}

		if (userList.size() == 0) {
			model.addAttribute("Error", "No User Found !!!");
		} else {
			model.addAttribute("userList", userList);
			model.addAttribute("Success", "Search done Successfully");
		}
		return "USR001";
	}

	@GetMapping(value = "/setupAddUser")
	public ModelAndView addUser() {

		return new ModelAndView("USR002", "uBean", new UserBean());
	}

	@PostMapping(value = "/addUser")
	public String addUser(@ModelAttribute("uBean") @Validated UserBean userBean, BindingResult br, ModelMap model) {
		if (br.hasErrors()) {
			return "USR002";
		}
		UserDTO uDTO = new UserDTO();

		if (userBean.getPassword().equals(userBean.getConfirm())) {

			uDTO.setId(userBean.getId());

			List<UserDTO> checkUserList = uService.findByIdOrName(uDTO.getId(), uDTO.getName());

			if (checkUserList.size() != 0) {
				model.addAttribute("Error", "User ID has been already used..... Choose another user ID");
			} else {
				uDTO.setName(userBean.getName());

				uDTO.setPassword(userBean.getPassword());

				uDTO.setRole("ROLE_USER");
				uDTO.setEnable(1);
				uService.save(uDTO);

				List<UserDTO> list = uService.findByIdOrName(uDTO.getId(), uDTO.getName());
				int i = list.size();

				if (i > 0) {
					model.addAttribute("Success", "User registered Successfully");
				} else {
					model.addAttribute("Error", "User register fail !!!");
				}
			}
		} else {
			model.addAttribute("Error", "Passwords didn't match !!!");
		}
		return "USR002";
	}

	@GetMapping(value = "setupUpdateUser")
	public ModelAndView setupUpdateUser(@RequestParam String id) {
		UserDTO uDTO = new UserDTO();
		uDTO.setId(id);

		List<UserDTO> list = uService.findByIdOrName(uDTO.getId(), uDTO.getName());
		UserBean userBean = new UserBean();
		for (UserDTO upDTO : list) {
			userBean.setId(upDTO.getId());
			userBean.setName(upDTO.getName());
			userBean.setPassword(upDTO.getPassword());
			userBean.setConfirm(upDTO.getPassword());
		}
		return new ModelAndView("USR002-01", "uBean", userBean);
	}

	@PostMapping(value = "updateUser")
	public String updateUser(@ModelAttribute("uBean") @Validated UserBean userBean, BindingResult br, ModelMap model) {
		if (br.hasErrors()) {
			return "USR002-01";
		}

		UserDTO uDTO = new UserDTO();
		if (userBean.getPassword().equals(userBean.getConfirm())) {
			uDTO.setId(userBean.getId());
			uDTO.setName(userBean.getName());
			uDTO.setPassword(userBean.getPassword());

			uService.save(uDTO);
			List<UserDTO> list = uService.findByIdOrName(uDTO.getId(), uDTO.getName());
			int i = list.size();

			if (i > 0) {
				model.addAttribute("Success", "User Updated Successfully");
			}
		} else {
			model.addAttribute("Error", "Password didn't match !!!");
		}
		return "USR002-01";

	}

	@GetMapping(value = "/deleteUser")
	public String deleteUser(@RequestParam String id, RedirectAttributes redir, ModelMap model,
			HttpServletRequest session) {
		UserDTO uDTO = new UserDTO();
		uDTO.setId(id);

		if (uDTO.getId().equals(session.getRemoteUser())) {
			redir.addAttribute("Error", "Cann't delete this current login user !!!");
		} else {

			uService.deleteById(id);
			List<UserDTO> list = uService.findByIdOrName(id, "");
			int i = list.size();
			if (i == 0) {
				redir.addAttribute("Success", "Deleted " + id + " Successfully");
			}

		}
		return "redirect:/user/setupUser";
	}
}
