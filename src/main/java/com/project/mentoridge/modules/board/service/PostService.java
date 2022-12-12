package com.project.mentoridge.modules.board.service;

import com.project.mentoridge.config.exception.EntityNotFoundException;
import com.project.mentoridge.config.exception.UnauthorizedException;
import com.project.mentoridge.modules.account.repository.UserRepository;
import com.project.mentoridge.modules.account.vo.User;
import com.project.mentoridge.modules.base.AbstractService;
import com.project.mentoridge.modules.board.controller.request.PostCreateRequest;
import com.project.mentoridge.modules.board.controller.request.PostUpdateRequest;
import com.project.mentoridge.modules.board.controller.response.PostResponse;
import com.project.mentoridge.modules.board.repository.*;
import com.project.mentoridge.modules.board.vo.Liking;
import com.project.mentoridge.modules.board.vo.Post;
import com.project.mentoridge.modules.log.component.LikingLogService;
import com.project.mentoridge.modules.log.component.PostLogService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.project.mentoridge.config.exception.EntityNotFoundException.EntityType.POST;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService extends AbstractService {

    private final PostRepository postRepository;
    private final PostQueryRepository postQueryRepository;
    private final ContentSearchRepository contentSearchRepository;
    private final PostLogService postLogService;

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikingRepository likingRepository;
    private final LikingLogService likingLogService;

        private User getUser(String username) {
            return userRepository.findByUsername(username).orElseThrow(UnauthorizedException::new);
        }

        private Post getPost(User user, Long postId) {
            return postRepository.findByUserAndId(user, postId)
                    .orElseThrow(() -> new EntityNotFoundException(POST));
        }

        private Post getPost(Long postId) {
            return postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException(POST));
        }
        
        // -> Locking 방지를 위해 테이블로 분리 : insert로 해결
        // -> 조회 시마다 Liking 카운트
        // -> 조회 시 성능 이슈 발생
        // TODO - CHECK
        // - 좋아요 수는 높은 정합성을 요구하는 데이터인가? 아니다
        // - 스케줄러로 특정 주기마다 count를 계산해서 Post 테이블에 좋아요 수 update
        private void setCounts(Page<PostResponse> postResponses) {
            List<Long> postIds = postResponses.stream().map(PostResponse::getPostId).collect(Collectors.toList());
            Map<Long, Long> postCommentQueryDtoMap = postQueryRepository.findPostCommentQueryDtoMap(postIds);
            Map<Long, Long> postLikingQueryDtoMap = postQueryRepository.findPostLikingQueryDtoMap(postIds);

            postResponses.stream()
                    .forEach(postResponse -> {
                        Long postId = postResponse.getPostId();
                        if (postCommentQueryDtoMap.get(postId) != null) {
                            postResponse.setCommentCount(postCommentQueryDtoMap.get(postId));
                        } else {
                            postResponse.setCommentCount(0L);
                        }
                        if (postLikingQueryDtoMap.get(postId) != null) {
                            postResponse.setLikingCount(postLikingQueryDtoMap.get(postId));
                        } else {
                            postResponse.setLikingCount(0L);
                        }
                    });
        }

        private void setCount(PostResponse postResponse) {
            Long postId = postResponse.getPostId();
            Long commentCount = postQueryRepository.findPostCommentQueryDtoMap(postId);
            if (commentCount != null) {
                postResponse.setCommentCount(commentCount);
            } else {
                postResponse.setCommentCount(0L);
            }
            Long likingCount = postQueryRepository.findPostLikingQueryDtoMap(postId);
            if (likingCount != null) {
                postResponse.setLikingCount(likingCount);
            } else {
                postResponse.setLikingCount(0L);
            }

        }

        private void setLiked(User user, Long postId, PostResponse response) {

            if (user == null) {
                return;
            }
            Post post = getPost(postId);
            Optional<Liking> liking = Optional.ofNullable(likingRepository.findByUserAndPost(user, post));
            response.setLiked(liking.isPresent());
        }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostResponsesOfUser(User user, Integer page) {

        user = getUser(user.getUsername());
        Page<PostResponse> postResponses = postRepository.findByUser(user, getPageDescRequest(page)).map(PostResponse::new);

        setCounts(postResponses);
        return postResponses;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostResponses(User user, Integer page) {

        // user = getUser(user.getUsername());
        Page<PostResponse> postResponses = postRepository.findAll(getPageDescRequest(page)).map(PostResponse::new);
        setCounts(postResponses);
        return postResponses;
    }

    // TODO - Post, Comment + 닉네임(?) 둘 다 search
    // Post title/content, Comment content
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostResponses(User user, String search, Integer page) {

        Page<PostResponse> postResponses = null;
        if (!StringUtils.isBlank(search)) {
            postResponses = contentSearchRepository.findPostsSearchedByContent(search, getPageDescRequest(page));
        } else {
            postResponses = getPostResponses(user, page);
        }
        setCounts(postResponses);
        return postResponses;
    }

    @Transactional
    public PostResponse getPostResponse(User user, Long postId) {

        user = getUser(user.getUsername());
        // TODO : 동시성 이슈 체크
        // select ~ for update
        Post post = getPost(postId);
        post.hit();

        PostResponse postResponse = new PostResponse(post);
        setCount(postResponse);
        setLiked(user, postId, postResponse);
        return postResponse;
    }

    // 댓글단 글 리스트
    @Transactional(readOnly = true)
    public Page<PostResponse> getCommentingPostResponses(User user, Integer page) {

        Page<PostResponse> posts = postQueryRepository.findCommentingPosts(user.getId(), getPageRequest(page));
        setCounts(posts);
        return posts;
    }

    // 좋아요한 글 리스트
    @Transactional(readOnly = true)
    public Page<PostResponse> getLikingPostResponses(User user, Integer page) {

        Page<PostResponse> posts = postQueryRepository.findLikingPosts(user.getId(), getPageRequest(page));
        setCounts(posts);
        return posts;
    }

    public Post createPost(User user, PostCreateRequest createRequest) {

        user = getUser(user.getUsername());
        Post saved = postRepository.save(createRequest.toEntity(user));
        postLogService.insert(user, saved);
        return saved;
    }

    public void updatePost(User user, Long postId, PostUpdateRequest updateRequest) {

        user = getUser(user.getUsername());
        Post post = getPost(user, postId);
        post.update(updateRequest, user, postLogService);
    }

    public void deletePost(User user, Long postId) {

        user = getUser(user.getUsername());
        Post post = getPost(user, postId);

//        List<Comment> comments = commentRepository.findByPost(post);
//        List<Liking> likings = likingRepository.findByPost(post);
//        post.addComments(comments);
//        post.addLikings(likings);

        post.delete(user, postLogService);

        commentRepository.deleteByPost(post);
        likingRepository.deleteByPost(post);
        postRepository.delete(post);
    }

    public void likePost(User user, Long postId) {

        user = getUser(user.getUsername());
        Post post = getPost(postId);

        Liking liking = likingRepository.findByUserAndPost(user, post);
        if (liking == null) {
            Liking saved = likingRepository.save(Liking.builder()
                    .user(user)
                    .post(post)
                    .build());
            likingLogService.insert(user, saved);
        } else {
            liking.delete(user, likingLogService);
            likingRepository.delete(liking);
        }
    }

}
