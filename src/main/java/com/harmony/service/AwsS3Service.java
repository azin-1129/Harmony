package com.harmony.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.harmony.entity.User;
import com.harmony.global.response.code.ErrorCode;
import com.harmony.global.response.exception.EntityNotFoundException;
import com.harmony.global.response.exception.UploadFailedException;
import com.harmony.repository.UserRepository;
import com.harmony.security.impl.UserDetailsImpl;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AwsS3Service {
  private final UserRepository userRepository;
  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  private final AmazonS3 amazonS3;
  private final Tika tika=new Tika();
  private final List<String> validImageTypes= Arrays.asList("image/png", "image/jpeg");

  // TODO: 업로드 시 리사이징, 업데이트
  public String uploadProfileImage(UserDetailsImpl userDetailsImpl, MultipartFile multipartFile){
    String originalProfileImageName=userDetailsImpl.getProfileImageName();
    // TODO: User profileImageName 초기값은 null로 바꿔야함
    if(!originalProfileImageName.equals("")){ // 기존 프사가 존재해서 삭제
      log.info("기존 프사가 존재하네요?"+originalProfileImageName);
      deleteProfileImage(originalProfileImageName);
    }
    ObjectMetadata objectMetadata=new ObjectMetadata();
    objectMetadata.setContentType(validImageType(multipartFile));
    objectMetadata.setContentLength(multipartFile.getSize());

    String fileName=createFileName(multipartFile.getOriginalFilename());
    log.info("생성된 파일 이름입니다:"+fileName);
    try(InputStream inputStream=multipartFile.getInputStream()){
      amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
          .withCannedAcl(CannedAccessControlList.PublicRead));

      User user=userRepository.findById(userDetailsImpl.getUserId()).orElseThrow(
          ()->new EntityNotFoundException(ErrorCode.USER_NOT_FOUND)
      );
      user.updateProfileImage(fileName);
    }catch(IOException e){
      throw new UploadFailedException(
          ErrorCode.FILE_UPLOAD_FAILED
      );
    }

    return fileName;
  }
  private String validImageType(MultipartFile multipartFile){
    try{
      InputStream inputStream=multipartFile.getInputStream();
      String mimeType=tika.detect(inputStream);

      // JPEG, PNG 외 거부
      if(!validImageTypes.contains(mimeType)){
        throw new UploadFailedException(
            ErrorCode.INVALID_FILE
        );
      }

      return mimeType;

      } catch(IOException e){
      throw new UploadFailedException(
          ErrorCode.INVALID_FILE
      );
    }
  }

  public void deleteProfileImage(String fileName){
    amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
  }

  public String createFileName(String fileName){
    log.info("원래 파일 이름입니다:"+fileName);
    return UUID.randomUUID().toString().concat(".").concat(FilenameUtils.getExtension(fileName));
  }
}
