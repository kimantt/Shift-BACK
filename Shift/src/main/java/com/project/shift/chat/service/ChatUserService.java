package com.project.shift.chat.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import com.project.shift.chat.dto.ChatUserMyPageInfoDTO;
import com.project.shift.chat.dto.ChatUserSearchResultDTO;
import com.project.shift.chat.entity.ChatUserEntity;
import com.project.shift.chat.repository.ChatUserRepository;
import com.project.shift.chat.repository.FriendRepository;
import com.project.shift.global.exception.detail.user.UserNotFoundException;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatUserService {

	private final ChatUserRepository chatUserRepository;
	private final FriendRepository friendRepository;
	
//	@Value("${cloud.aws.s3.bucket}")
//	private String bucketName;
//	
//	@Value("${cloud.aws.region}")
//	private String region;
//	
//	@Value("${cloud.aws.credentials.access-key}")
//	private String accessKey;
//	
//	@Value("${cloud.aws.credentials.secret-key}")
//	private String secretKey;
//    
//    private S3Client s3;
//
//    @PostConstruct
//    public void initS3() {
//        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
//
//        this.s3 = S3Client.builder()
//                .region(Region.of(region))
//                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
//                .build();
//    }
	
	@Transactional(readOnly = true)
	public ChatUserSearchResultDTO searchUserByPhone(long userId, String phone) {
		ChatUserEntity entity = chatUserRepository.findByPhoneFlexible(phone);
		if (entity == null) {
	        throw new UserNotFoundException("해당 전화번호의 사용자를 찾을 수 없습니다.");
		}
		// 검색된 사용자와의 친구여부 포함하여 반환
		long friendId = entity.getUserId();
		boolean ifFriend = friendRepository.existsByUserIdAndFriendId(userId, friendId);
		
		return ChatUserSearchResultDTO.builder()
				.ifFriend(ifFriend)
				.userId(entity.getUserId())
				.loginId(entity.getLoginId())
				.name(entity.getName())
				.phone(entity.getPhone())
				.build();
	}
	
	@Transactional(readOnly = true)
	public Optional<ChatUserMyPageInfoDTO> getChatUserInfo(long userId) {
		Optional<ChatUserEntity> entity = chatUserRepository.findById(userId);
		if (!entity.isEmpty()) {
			ChatUserEntity e = entity.get();
			return Optional.of(ChatUserMyPageInfoDTO.builder()
					.id(e.getLoginId())
					.name(e.getName())
					.phone(e.getPhone())
					.build());
		}
		return Optional.empty();
	}
	
	// 프로필 이미지 업로드
	@Transactional
	public void uploadProfileImage(long userId, MultipartFile file) {
//	    if (file == null || file.isEmpty()) {
//	        throw new IllegalArgumentException("업로드할 파일이 없습니다.");
//	    }
//
//	    String key = "user_profile/" + userId + ".png";
//
//	    try {
//	        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//	                .bucket(bucketName)
//	                .key(key)
//	                .contentType(file.getContentType())
//	                .build();
//
//	        s3.putObject(putObjectRequest,
//	                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
//	                        file.getInputStream(),
//	                        file.getSize()
//	                ));
//
//	    } catch (Exception e) {
//	        throw new RuntimeException("S3 업로드 실패", e);
//	    }
		throw new RuntimeException("S3 업로드 실패");
	}
	
}
