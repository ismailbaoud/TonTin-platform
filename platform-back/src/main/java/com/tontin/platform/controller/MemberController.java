package com.tontin.platform.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;
import com.tontin.platform.service.impl.MemberServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {
    
    private final MemberServiceImpl memberService; 

    @PostMapping("/{id}")
    public ResponseEntity<MemberResponse> addMember(@RequestBody MemberRequest request,@PathVariable UUID id ,@RequestParam("dartId") UUID dartId) {
        return ResponseEntity.ok().body(memberService.addMemberToDart(request, id , dartId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(@RequestBody MemberRequest request, @PathVariable UUID id, @RequestParam("dartId") UUID dartId) {
        return ResponseEntity.ok().body(memberService.updateMemberPermession(request, id, dartId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable("id") UUID id, @RequestParam("dartId") UUID dartId) {
        return ResponseEntity.ok().body(memberService.getMember(id, dartId));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<MemberResponse>> getAllMembers(@RequestParam("dartId") UUID dartId) {
        return ResponseEntity.ok().body(memberService.getAllMembersOfDart(dartId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMember(@PathVariable("id") UUID id, @RequestParam("dartId") UUID dartId) {
        return ResponseEntity.ok().body(memberService.deleteMember(id, dartId));
    } 


}
