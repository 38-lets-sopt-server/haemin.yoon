package org.sopt.service;

public class PostValidator {

  // 기존 PostService 안에 있던 제목/내용 검증을 여기로 뺀다.

  // 게시글 생성 검증
  public void validateCreate(String title, String content, String author) {
    validateTitle(title);
    validateContent(content);
    validateAuthor(author);
  }

  // 게시글 수정 검증
  public void validateUpdate(String title, String content) {
    validateTitle(title);
    validateContent(content);
  }

  private void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("제목은 필수입니다!");
    }
  }

  private void validateContent(String content) {
    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("내용은 필수입니다!");
    }
  }

  private void validateAuthor(String author) {
    if (author == null || author.isBlank()) {
      throw new IllegalArgumentException("작성자는 필수입니다!");
    }
  }
}