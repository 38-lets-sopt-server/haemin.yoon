package org.sopt.domain.post.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sopt.domain.post.entity.BoardType;
import org.sopt.domain.post.entity.Post;


public interface PostRepository {

  Post save(Post post);

  List<Post> findAll();

  List<Post> findAllByBoardType(BoardType boardType);

  Optional<Post> findById(Long id);

  boolean deleteById(Long id);

  Long generateId();
}
