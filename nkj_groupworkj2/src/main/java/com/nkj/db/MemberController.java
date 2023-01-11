package com.nkj.db;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.Data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.nkj.db.MemberModel;
import com.nkj.db.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController

public class MemberController {
	@Autowired
	MemberModel memberModel;
	@Autowired
	MemberRepository userDAO;

	@RequestMapping("/createMember")
	public RedirectView create(@RequestParam String data[], RedirectAttributes attributes) throws IOException {
		memberModel = new MemberModel();
		memberModel.setEmail(data[0]);
		String email = data[0];
		String password = data[1];

		List<MemberModel> selectMember = userDAO.selectMember(memberModel);

		if (selectMember.size() > 0) {
			attributes.addAttribute("message", "帳號已存在");
			return new RedirectView("signin");
		} else if (!isValidPassword(password)) { // 檢查密碼是否符合密碼強度要求
			attributes.addAttribute("message", "帳號或密碼格式不正確，請重新輸入");
			return new RedirectView("signin");
		} else if (!isValidEmail(email)) { // 檢查帳號是否為有效的電子郵件地址
			attributes.addAttribute("message", "帳號或密碼格式不正確，請重新輸入");
			return new RedirectView("signin");
		} else {
			memberModel.setEmail(email);
			memberModel.setPassword(password);
			userDAO.insert(memberModel);
//			redirectAttrs.addFlashAttribute("message", "已成功註冊帳號");
			attributes.addAttribute("message", "已成功註冊帳號");
			return new RedirectView("/");
		}
	}

	@RequestMapping("/getPassword")
	public String sql(@RequestParam String[] email) {
		memberModel = new MemberModel();
		memberModel.setEmail(email[0]);

		List<MemberModel> selectMember = userDAO.selectMember(memberModel);
		if (selectMember.size() > 0) {
			return "您的密碼是:" + selectMember.get(0).getPassword();// 成功
		} else {
			return "查無此帳號";
		}
	}

	@RequestMapping("/doLogin")
	public void doLogin(@RequestParam String[] data, HttpSession session, HttpServletResponse response)
			throws IOException {
		String email = data[0];
		String password = data[1];
		String checkEmail = null;
		String checkPassword = null;
//		PrintWriter out =response.getWriter();
		MemberModel input = new MemberModel();
		input.setEmail(email);

		List<MemberModel> selectMember = userDAO.selectMember(input);

		if (selectMember.size() > 0) {
			checkEmail = selectMember.get(0).getEmail();
			checkPassword = selectMember.get(0).getPassword();
		}
		System.out.println(selectMember);
		System.out.println(password);
		System.out.println(checkEmail);
		System.out.println(checkPassword);
		if (password.length() == 0 || email.length() == 0) {
			System.out.println("沒有輸入帳號或密碼!!");
//			out.print("<script>alert(\"帳號或密碼格式不正確，請重新輸入\");</script>");
			response.sendRedirect("/login");
		} else if (password.equals(checkPassword) && email.equals(checkEmail)) {
			session.setAttribute("uid", email);
//			Cookie cookie = new Cookie("SESSIONID", session.getId());
//			response.addCookie(cookie);
			System.out.println("成功登入!");
			response.sendRedirect("/");
		} else {
			System.out.println("帳號或密碼錯誤!!");
			System.out.println(password);
			response.sendRedirect("/login");
		}
	}

	@RequestMapping("/doApiLogin")
	public void doApiLogin(@RequestParam String email, @RequestParam String password, @RequestParam String loginstatus,
			HttpSession session, HttpServletResponse response) throws IOException {
		MemberModel input = new MemberModel();
		input.setEmail(email);
		List<MemberModel> selectMember = userDAO.selectMember(input);
		String check = selectMember.get(0).getPassword();
		System.out.println(password);
		System.out.println(check);

		if (password.equals(check)) {
			session.setAttribute("uid", email);
			System.out.println("成功登入!");
			response.sendRedirect("/");
		} else if (loginstatus == "true") {
			System.out.println("第三方登入!!");
			session.setAttribute("session_email", email);
			session.setAttribute("session_password", password);
			session.setAttribute("uid", email);
			response.sendRedirect("/createMember");
		} else {
			System.out.println("登入失敗!!");
			response.sendRedirect("/login");
		}
	}

	private boolean isValidEmail(String email) {
		// 使用正則表達式檢查電子郵件地址是否合法
		String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		if (!email.matches(regex)) {
			// 密碼長度不符合要求
			return false;
		}
		return true;
	}

	private boolean isValidPassword(String password) {
		// 檢查密碼是否符合密碼強度要求
		// 例如，檢查密碼長度、是否包含大小寫
		String regex = "^.{8,100}$"; // 設置正則表達式
		if (!password.matches(regex)) {
			// 密碼長度不符合要求
			return false;
		}
		return true;
	}

	@RequestMapping("/deletesession")
	public void deletesession(HttpSession session, HttpServletResponse response) throws IOException {
		session.removeAttribute("uid");
		response.sendRedirect("/");
	}

}
