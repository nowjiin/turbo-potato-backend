package com.hotspot.livfit.challenge.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotspot.livfit.challenge.entity.ChallengeEntity;
import com.hotspot.livfit.challenge.entity.ChallengeUserEntity;
import com.hotspot.livfit.challenge.repository.ChallengeUserRepository;
import com.hotspot.livfit.challenge.service.ChallengeService;
import com.hotspot.livfit.user.util.JwtUtil;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/api/challenge")
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {

  private final ChallengeService challengeService;
  private final JwtUtil jwtUtil;
  private final ChallengeUserRepository challengeUserRepository;
  /*
   * URL: api/challenge/show/all
   * HTTP Method: GET
   * 토큰 필요 x
   */

  //  프론트 엔드가 사용하게 전체 리스트
  @GetMapping("/show/all")
  public ResponseEntity<List<ChallengeEntity>> getAllChallenge() {
    List<ChallengeEntity> challengeEntity = challengeService.findAllChallenges();
    return ResponseEntity.ok(challengeEntity);
  }

  /*
   * URL: api/challenge/show/all
   * HTTP Method: GET
   * 토큰 필요 x
   */

  // 번호에 맞춰서 프론트 엔드가 사용하게
  @GetMapping("/show/{id}")
  public ResponseEntity<ChallengeEntity> getChallenge(@PathVariable Long id) {
    Optional<ChallengeEntity> challenge = challengeService.getChallenge(id);
    return challenge.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  // 챌린지 기록 저장하기 (성공 실패 여부)
  // 챌린지 기록 저장하기 (성공 실패 여부)
  @PostMapping("/save_challenge")
  public ResponseEntity<?> saveChallenge(
      @RequestHeader("Authorization") String bearerToken,
      @RequestBody ChallengeUserEntity challengeUserEntity) {

    if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Invalid Authorization header format.");
    }
    String token = bearerToken.substring(7).trim();
    if (token.isEmpty() || token.split("\\.").length != 3) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JWT token format.");
    }

    try {
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      String jwtLoginId = claims.getId(); // JWT에서 사용자 ID를 추출
      LocalDateTime startedAt = LocalDateTime.now();

      log.info("Received challengeUserEntity: {}", challengeUserEntity);
      log.info("Challenge ID: {}", challengeUserEntity.getChallenge().getId());
      log.info("Challenge Title: {}", challengeUserEntity.getChallenge().getTitle());
      log.info("User ID: {}", challengeUserEntity.getUser().getId());
      log.info("Success: {}", challengeUserEntity.getSuccess());

      // ChallengeUserEntity에서 ChallengeEntity의 ID를 가져옴
      Long challengeId = challengeUserEntity.getChallenge().getId();

      // ChallengeService를 통해 챌린지 기록을 저장
      ChallengeUserEntity savedChallenge =
          challengeService.saveChallenge(
              jwtLoginId, challengeId, startedAt, challengeUserEntity.getSuccess());

      return ResponseEntity.ok(savedChallenge);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred while saving the challenge record: " + e.getMessage());
    }
  }
  // 마이페이지 챌린지 기록 가져오기 (성공, 실패, 진행중)
  @GetMapping("/get_challenge_record")
  public ResponseEntity<List<ChallengeUserEntity>> getAllRecords(
      @RequestHeader("Authorization") String bearerToken) {
    try {
      // Bearer 토큰에서 JWT 추출
      String token = bearerToken.substring(7);
      // 모든 클레임 추출
      Claims claims = jwtUtil.getAllClaimsFromToken(token);
      // 클레임에서 로그인 아이디 추출 -> 로그인 아이디로 사용자 뱃지 가져오기
      String jwtLoginId = claims.getId();

      // 로그인 아이디로 사용자 챌린지 기록 조회
      List<ChallengeUserEntity> challengeEntities =
          challengeService.getChallengeUserById(jwtLoginId);
      return ResponseEntity.ok(challengeEntities);
    } catch (RuntimeException e) {
      log.error(
          "Error during fetching Lunge records in controller /api/lunge/get_my_record: {}",
          e.getMessage());
      return ResponseEntity.status(500).body(null);
    }
  }
}
