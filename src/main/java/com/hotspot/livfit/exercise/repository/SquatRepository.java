package com.hotspot.livfit.exercise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotspot.livfit.exercise.entity.Squat;

public interface SquatRepository extends JpaRepository<Squat, Long> {
  List<Squat> findByUserId(Long userId); // 특정 유저의 기록을 조회하는 메서드
}
