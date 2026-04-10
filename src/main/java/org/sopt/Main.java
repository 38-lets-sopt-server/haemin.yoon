package org.sopt;

import java.util.List;
import java.util.Scanner;
import org.sopt.controller.PostController;
import org.sopt.dto.ApiResponse;
import org.sopt.dto.request.CreatePostRequest;
import org.sopt.dto.response.CreatePostResponse;
import org.sopt.dto.response.PostResponse;

public class Main {
  public static void main(String[] args) {
    // 클라이언트는 Controller만 알면 돼요. Service도 Repository도 몰라도 돼요.
    PostController postController = new PostController();
    Scanner scanner = new Scanner(System.in);
    boolean running = true;

    while (running) {
      System.out.println("\n=== 에브리타임 게시판 ===");
      System.out.println("1. 게시글 작성");
      System.out.println("2. 전체 조회");
      System.out.println("3. 단건 조회");
      System.out.println("4. 게시글 수정");
      System.out.println("5. 게시글 삭제");
      System.out.println("0. 종료");
      System.out.print("메뉴 선택: ");

      int choice = scanner.nextInt();
      scanner.nextLine();

      switch (choice) {
        case 1:
          System.out.print("제목: ");
          String title = scanner.nextLine();
          System.out.print("내용: ");
          String content = scanner.nextLine();
          System.out.print("작성자: ");
          String author = scanner.nextLine();
          // 클라이언트가 요청 객체를 만들어서 Controller에 전달
//          CreatePostResponse response = postController.createPost(
//              new CreatePostRequest(title, content, author)
//          );
//          System.out.println(response.message);
          ApiResponse<CreatePostResponse> createResponse =
              postController.createPost(new CreatePostRequest(title, content, author));

          System.out.println(createResponse.message);
          if (createResponse.success && createResponse.data != null) {
            System.out.println("생성된 게시글 ID: " + createResponse.data.id);
          }
          break;

        case 2:
//          List<PostResponse> posts = postController.getAllPosts();
//          if (posts.isEmpty()) {
//            System.out.println("등록된 게시글이 없습니다.");
//          } else {
//            posts.forEach(p -> System.out.println(p + "\n---"));
//          }
          ApiResponse<List<PostResponse>> allResponse = postController.getAllPosts();
          if (!allResponse.success) {
            System.out.println(allResponse.message);
            break;
          }
          List<PostResponse> posts = allResponse.data;
          if (posts.isEmpty()) {
            System.out.println("등록된 게시글이 없습니다.");
          } else {
            posts.forEach(p -> System.out.println(p + "\n---"));
          }
          break;

        case 3:
          System.out.print("조회할 게시글 ID: ");
//          PostResponse post = postController.getPost(scanner.nextLong());
//          scanner.nextLine();
//          if (post != null) System.out.println(post);
          Long readId = scanner.nextLong();
          scanner.nextLine();

          ApiResponse<PostResponse> readResponse = postController.getPost(readId);
          System.out.println(readResponse.message);
          if (readResponse.success && readResponse.data != null) {
            System.out.println(readResponse.data);
          }
          break;

        case 4:
          System.out.print("수정할 게시글 ID: ");
          Long updateId = scanner.nextLong();
          scanner.nextLine();
          System.out.print("새 제목: ");
          String newTitle = scanner.nextLine();
          System.out.print("새 내용: ");
          String newContent = scanner.nextLine();
          // postController.updatePost(updateId, newTitle, newContent);
          ApiResponse<Void> updateResponse = postController.updatePost(updateId, newTitle, newContent);
          System.out.println(updateResponse.message);
          break;

        case 5:
          System.out.print("삭제할 게시글 ID: ");
//          postController.deletePost(scanner.nextLong());
//          scanner.nextLine();
          Long deleteId = scanner.nextLong();
          scanner.nextLine();

          ApiResponse<Void> deleteResponse = postController.deletePost(deleteId);
          System.out.println(deleteResponse.message);
          break;

        case 0:
          running = false;
          System.out.println("👋 프로그램 종료");
          break;
        default:
          System.out.println("❗ 잘못된 입력입니다.");
      }
    }
    scanner.close();
  }
}