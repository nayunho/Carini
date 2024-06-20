package com.car.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.request.RequestAttributes;

import com.car.validation.Find_idFormValidation;
import com.car.validation.Find_pwFormValidation;
import com.car.validation.LoginFormValidation;
import com.car.validation.SignupFormValidation;
import com.car.dto.Member;
import com.car.persistence.MemberRepository;
import com.car.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.apache.commons.lang3.RandomStringUtils;
@Controller
@SessionAttributes("user")
public class LoginController {

	@Value("${pw-role.password-rejex}")
	private String passwordRegex;

	@Value("${coolsms.api.key}")
	private String APIKEY;
	
	@Value("${coolsms.api.secret}")
	private String SECRETKEY;
	
	@Value("${coolsms.api.form_number}")
	private String FROM_NUMBER;
	
    @Autowired
	private MemberService memberService;
	
    @ModelAttribute("member")
	public Member setMember() {
		return new Member(); // 기본 Member 객체를 세션에 저장
	}
    
    /*세션 초기화 */
	@GetMapping("/")
	public String backhome(HttpServletRequest request) {
		// 세션을 삭제
		HttpSession session = request.getSession(false);
		// session이 null이 아니라는건 기존에 세션이 존재했었다는 뜻이므로
		// 세션이 null이 아니라면 session.invalidate()로 세션 삭제해주기.
		if (session != null) {
			session.invalidate();
		}
		return "index.html";
	}
    
    /*
     * 회원가입 view
     * */
	@GetMapping("/signup")
	public String joinView(@ModelAttribute("SignupFormValidation") SignupFormValidation member) {
		return "member/signup.html";
	}
	
	@GetMapping("/test")
    public String getTestPage(Model model) {
        return "base/test.html";
    }
	

    /*
     * 회원가입 
     * */
	@PostMapping("/signup")
	public String join_result(@Validated @ModelAttribute("SignupFormValidation") SignupFormValidation member,BindingResult bindingResult,Model model) {
			
			if(bindingResult.hasErrors()) {
				return "member/signup";
			}
			
			
			List<Member> findmemberEmail=memberService.findByMemberEmail(member.getMemberEmail());
			List<Member> findmemberNickname=memberService.findByMemberNickname(member.getMemberNickname());
			Member findmemberId=memberService.findByMemberId(member.getMemberId());
			List<Member> findmemberPhone = memberService.findByMemberPhoneNum(member.getMemberPhoneNum());
			/*
			 * 이메일중복검사
			 * */
			if(!findmemberEmail.isEmpty()) {
				bindingResult.rejectValue("memberEmail",null, "존재하는 이메일입니다."); 
				return "member/signup";
			}
			/*
			 * 닉네임 중복검사
			 * */
			if(!findmemberNickname.isEmpty()) {
				bindingResult.rejectValue("memberNickname",null, "존재하는 닉네임입니다."); 
				return "member/signup";
			}
			/*
			 * 아이디중복검사
			 * */
			if(findmemberId != null && findmemberId.getMemberId().equals(member.getMemberId())) {
				bindingResult.rejectValue("memberId", null, "존재하는 아이디입니다."); 
				return "member/signup";
			}
			/*
			 * 전화번호 중복검사
			 * */
			if(!findmemberPhone.isEmpty()) {
				bindingResult.rejectValue("memberPhoneNum", null, "존재하는 전화번호입니다"); 
				return "member/signup";
			}
			Member Member = new Member();
			Member.setMemberEmail(member.getMemberEmail());
			Member.setMemberId(member.getMemberId());
			Member.setMemberPw(member.getMemberPw());
			Member.setMemberName(member.getMemberName());
			Member.setMemberNickname(member.getMemberNickname());
			Member.setMemberPhoneNum(member.getMemberPhoneNum());
			Member.setMemberSocial("회원");
			Member.setMemberRole("사용자");
				
			Member save_member=memberService.insertMember(Member);
				
			model.addAttribute("msg", "성공적으로 회원가입이 되었습니다.");
            model.addAttribute("url", "/member_login");	
            return "alert";	
               
	}
	
	/*
	 * 로그인 view
	 * */
	@GetMapping("/member_login")
	public String loginView(@ModelAttribute("LoginFormValidation") LoginFormValidation memberm,
			@RequestParam(value="redirectURL",defaultValue = "/home") String redirectURL,
			Model model) {
		System.out.println(redirectURL);
		model.addAttribute("redirectURL", redirectURL);

		return "member/login.html";
	}
	
	@GetMapping("/home")
	public String goHome(HttpSession session)  {
//		System.out.println(member.getMemberId());
//		System.out.println(member.getMemberNickname());
//		System.out.println("-=============");
		// HttpSession session = request.getSession();	
		return "homepage/home.html";
	}
	
	/*
	 * 로그인
	 * */
	@PostMapping("/member_login_check")
	public String login_result(@Validated @ModelAttribute("LoginFormValidation") LoginFormValidation membercheck,BindingResult bindingResult ,
			@RequestParam(value="redirectURL",defaultValue = "/home") String redirectURL,
			Model model,HttpServletRequest request, HttpSession session) {

		String memberId = membercheck.getMemberId();
		String memberPw = membercheck.getMemberPw();
		System.out.println(memberId);
		if(bindingResult.hasErrors()) {
			return "member/login";
		}
		
		Member findmember = memberService.findMember(memberId);
		
		if(findmember==null) {
			bindingResult.rejectValue("memberId",null, "존재하지 않는 아이디입니다.");
			return "member/login";
		}
		
	     // 사용자가 존재하고 비밀번호가 일치하는지 확인
	     if (findmember != null && findmember.getMemberPw().equals(memberPw)) {
	    	 findmember.setMemberPw("*****");
	    	 findmember.setMemberPhoneNum("***-****-****");
	    	 findmember.setMemberEmail("****@****.***");
	    	 // 로그인 성공 시 세션에 멤버정보 저장하고 홈페이지로 이동
	    	 session.setAttribute("user", findmember);

	    	 System.out.println(redirectURL);
	    	 return "redirect:"+redirectURL;
	     }else{
	    	 bindingResult.rejectValue("memberPw",null, "비밀번호가 일치하지 않습니다.");
	    	 return "member/login";
	     }
	}
	
	/*
	 * 로그아웃
	 * */
	@PostMapping("/logout")
	public String logout(HttpServletRequest request) {
		//세션을 삭제
		HttpSession session = request.getSession(false); 
        // session이 null이 아니라는건 기존에 세션이 존재했었다는 뜻이므로
        // 세션이 null이 아니라면 session.invalidate()로 세션 삭제해주기.
		if(session != null) {
			session.invalidate();
		}
		return "redirect:/";
	}
	/*
	 * 아이디 찾기 폼
	 * */
	@GetMapping("/find_idForm")
	public String find_idForm(@ModelAttribute("Find_idFormValidation") Find_idFormValidation find_idFormValidation) {
		return "member/find_id";
	}
	/*
	 * 비밀번호 찾기 폼
	 * */
	@GetMapping("/find_pwForm")
	public String find_pwForm(@ModelAttribute("find_pwFormValidation") Find_pwFormValidation find_pwFormValidation) {
		
		return "member/find_pw";
	}
	
	/*
	 * 아이디찾기(인증번호 전송하기)
	 * */
	@PostMapping("/find_id")
	public ResponseEntity<Map<String, Object>> find_id(@Validated @ModelAttribute("find_idFormValidation") Find_idFormValidation find_idFormValidation,BindingResult bindingResult,
			HttpSession session,HttpServletRequest request) {
		
		Map<String, Object> response = new HashMap<>();

		if(bindingResult.hasErrors()) {
			Map<String, String> errors = new HashMap<>();
			// 필드별로 발생한 모든 오류 메시지를 맵에 담음
	        bindingResult.getFieldErrors().forEach(error -> {
	            String fieldName = error.getField();
	            String errorMessage = error.getDefaultMessage();
	            errors.put(fieldName, errorMessage);
	        });
			
			response.put("message", "회원정보오류");
			response.put("redirectUrl", "/find_idForm");
			response.put("success", false);
			response.put("errors",errors);
			return ResponseEntity.ok(response);
		}
		
		Member findmember = memberService.SMSfindMember(find_idFormValidation.getMemberName(),find_idFormValidation.getMemberPhoneNumber(),session);
		if(findmember ==null) {
			Map<String, String> errors = new HashMap<>();
			bindingResult.getFieldErrors().forEach(error -> {
	            String fieldName = error.getField();
	            String errorMessage = error.getDefaultMessage();
	            errors.put(fieldName, errorMessage);
	        });
			response.put("message", "회원정보가 일치하지 않습니다.");
	        response.put("success", false);
	        response.put("errors", "errors");
	        response.put("redirect", "/find_idForm");
	        return ResponseEntity.ok(response);
		}else if(find_idFormValidation.getMemberName() !=null && findmember.getMemberName().equals(find_idFormValidation.getMemberName())){
			if(findmember.getMemberPhoneNum().equals(find_idFormValidation.getMemberPhoneNumber())){
				sendmessage(find_idFormValidation.getMemberPhoneNumber(),request);
				response.put("success", true);
				response.put("message", "인증번호가 요청되었습니다.");
				return ResponseEntity.ok(response);
			}
		}
		response.put("message", "회원정보가 일치하지 않습니다.");
        response.put("success", false);
        response.put("redirect", "/find_idForm");
        return ResponseEntity.ok(response);
	}
	
	/*
	 * 비밀번호찾기(인증번호 전송하기)
	 * */
	@GetMapping("/find_pw")
	public ResponseEntity<Map<String, Object>> find_pw(@Validated @ModelAttribute("find_pwFormValidation") Find_pwFormValidation find_pwFormValidation,BindingResult bindingResult,
			HttpServletRequest request) {
		
		Map<String, Object> response = new HashMap<>();
		if(bindingResult.hasErrors()) {
			response.put("redirect", "/find_pwForm");
			return ResponseEntity.ok(response);
		}
		Member findmember = memberService.SMSfindMemberPw(find_pwFormValidation.getMemberId(),find_pwFormValidation.getMemberPhoneNumber());
		System.out.println(findmember);
		if(find_pwFormValidation.getMemberId() !=null && findmember.getMemberId().equals(find_pwFormValidation.getMemberId())) {
			if(findmember.getMemberPhoneNum().equals(find_pwFormValidation.getMemberPhoneNumber())){
				sendmessage(find_pwFormValidation.getMemberPhoneNumber(),request);
				
				response.put("success", true);
				return ResponseEntity.ok(response);
			}
		}else {
			response.put("message", "회원정보가 일치하지 않습니다.");
	        response.put("success", false);
	        response.put("redirect", "/find_pwForm");
	        return ResponseEntity.ok(response);
		}
		response.put("message", "회원정보가 일치하지 않습니다.");
        response.put("success", false);
        response.put("redirect", "/find_pwForm");
        return ResponseEntity.ok(response);
	}
	
	/*
	 * 아이디찾기(인증번호 확인)
	 * */
	@PostMapping("/find_id_code_check")
    public ResponseEntity<Map<String, Object>> find_id_code_check(@RequestParam("code") String code, HttpServletRequest request,Model model,HttpSession session) {
    	
    	Map<String, Object> response = new HashMap<>();

    	if(code.equals(session.getAttribute("codeNumber")) && session.getAttribute("find_idMember") != null) {
    		session.removeAttribute("code");
    		response.put("success", true);
    		response.put("memberId", ((Member)session.getAttribute("find_idMember")).getMemberId());
    		session.removeAttribute("find_idMember");
    		return ResponseEntity.ok(response);
    	}
    	else {
    		response.put("message", "인증번호가 일치하지 않습니다.");
    		response.put("success", false);
    		response.put("redirectUrl", "/find_idForm");
    		return ResponseEntity.ok(response);
    	}
    }
	@GetMapping("/find_pw_code_check")
    public ResponseEntity<Map<String, Object>> find_pw_code_check(@RequestParam String code, HttpServletRequest request,Model model) {
    	HttpSession session = request.getSession();
    	Map<String, Object> response = new HashMap<>();
    	if(code.equals(session.getAttribute("code")) && session.getAttribute("find_pwMember") != null) {
    		session.removeAttribute("code");
    		
    		response.put("success", true);
    		response.put("member", session.getAttribute("find_pwMember"));
    		response.put("redirect", "/member_login");
    		session.removeAttribute("find_pwMember");
    		return ResponseEntity.ok(response);
    	}
    	else {
    		response.put("msg", "인증번호가 일치하지 않습니다. 다시 입력헤주세요");
    		response.put("success", false);
    		response.put("redirect", "/find_pwForm");
    		return ResponseEntity.ok(response);
    	}
    }
	
	/*
	 * 인증번호 메세지 뿌리기
	 * */
	public SingleMessageSentResponse sendmessage(String phone,HttpServletRequest request) {
		String codeNumber=RandomStringUtils.randomNumeric(6);
		HttpSession session = request.getSession(false);
		session.setAttribute("codeNumber", codeNumber);
		System.out.println(session.getAttribute("codeNumber"));
		SingleMessageSentResponse response=memberService.sendmessage(phone,codeNumber,APIKEY,SECRETKEY,FROM_NUMBER);
		
		return response;
	}
	
}
