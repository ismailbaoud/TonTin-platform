package com.tontin.platform.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tontin.platform.domain.Member;

public interface MemberRepository extends JpaRepository<Member, UUID>{
    
}
