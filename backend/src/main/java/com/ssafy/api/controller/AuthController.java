package com.ssafy.api.controller;

import com.ssafy.api.request.ChangePasswordReq;
import com.ssafy.common.auth.CustomUserDetails;
import com.ssafy.db.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ssafy.api.request.UserLoginPostReq;
import com.ssafy.api.response.UserLoginPostRes;
import com.ssafy.api.service.UserService;
import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.common.util.JwtTokenUtil;
import com.ssafy.db.entity.User;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 인증 관련 API 요청 처리를 위한 컨트롤러 정의.
 */
@Api(value = "인증 API", tags = {"Auth."})
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	@Autowired
	UserService userService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	UserRepository userRepository;

	@PostMapping("/login")
	@ApiOperation(value = "로그인", notes = "<strong>아이디와 패스워드</strong>를 통해 로그인 한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = UserLoginPostRes.class),
		@ApiResponse(code = 401, message = "인증 실패", response = BaseResponseBody.class),
		@ApiResponse(code = 404, message = "사용자 없음", response = BaseResponseBody.class),
		@ApiResponse(code = 500, message = "서버 오류", response = BaseResponseBody.class)
	})
	public ResponseEntity<UserLoginPostRes> login(@RequestBody @ApiParam(value="로그인 정보", required = true) UserLoginPostReq loginInfo) {
		String userId = loginInfo.getId();
		String password = loginInfo.getPassword();

		User user = userService.getUserByUserId(userId);
		// 존재하지 않는 아이디일때
		if(user == null) {
			return ResponseEntity.status(404).body(UserLoginPostRes.of(404, "Unknown Id", null));
		}
		// 로그인 요청한 유저로부터 입력된 패스워드 와 디비에 저장된 유저의 암호화된 패스워드가 같은지 확인.(유효한 패스워드인지 여부 확인)
		if(passwordEncoder.matches(password, user.getPassword())) {
			// 유효한 패스워드가 맞는 경우, 로그인 성공으로 응답.(액세스 토큰을 포함하여 응답값 전달)
			return ResponseEntity.ok(UserLoginPostRes.of(200, "Success", JwtTokenUtil.getToken(userId)));
		}
		// 유효하지 않는 패스워드인 경우, 로그인 실패로 응답.
		return ResponseEntity.status(401).body(UserLoginPostRes.of(401, "Invalid Password", null));
	}

	@PatchMapping("/change-pwd")
	@ApiOperation(value = "비밀번호 변경", notes = "비밀번호를 변경한다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = BaseResponseBody.class),
		@ApiResponse(code = 401, message = "인증 실패", response = BaseResponseBody.class),
		@ApiResponse(code = 500, message = "서버 오류", response = BaseResponseBody.class)
	})
	public ResponseEntity<? extends BaseResponseBody> changePwd(@ApiIgnore Authentication authentication, @RequestBody @ApiParam(value = "변경할 비밀번호", required = true) ChangePasswordReq changePasswordReq) {

		try {
			CustomUserDetails userDetails = (CustomUserDetails)authentication.getDetails();
			String userId = userDetails.getUsername();
			User user = userService.getUserByUserId(userId);

			System.out.println(passwordEncoder.encode(changePasswordReq.getCur()));
			System.out.println(user.getPassword());

			if (passwordEncoder.matches(changePasswordReq.getCur(), user.getPassword())) {
				user.setPassword(passwordEncoder.encode(changePasswordReq.getPassword()));
				userRepository.save(user);
				return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
			} else {
				return ResponseEntity.status(400).body(BaseResponseBody.of(400, "password not valid"));
			}

		} catch (Exception e){
			return ResponseEntity.status(401).body(BaseResponseBody.of(401, "Failed : " + e));
		}

	}

}
