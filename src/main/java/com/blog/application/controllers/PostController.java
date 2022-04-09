package com.blog.application.controllers;

import com.blog.application.models.Comment;
import com.blog.application.models.Post;
import com.blog.application.models.Tag;
import com.blog.application.services.CommentService;
import com.blog.application.services.PostService;
import com.blog.application.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    @GetMapping("/")
    public String viewHomePage(Model model){
        return findPaginated(1, "publishedAt", "asc", model);
    }

    @GetMapping("/login")
    public String login(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }
        return "redirect:/";
    }

    @GetMapping("/showNewPostForm")
    public String showNewPostForm(Model model){
        Post posts = new Post();
        model.addAttribute("posts", posts);
        return "new_post";
    }

    @RequestMapping("/search")
    public String search(Model model, String keyword){
        List<Post> postsList;
        postsList = keyword == null? postService.findAll(): postService.findByKeyword(keyword);
        model.addAttribute("listOfPosts", postsList);
        return "index";
    }

    @RequestMapping("/filter")
    public String filter(Model model, @RequestParam("filter") String[] filter){
        List<Post> posts = postService.findAll();
        Set<String> author = new HashSet<>();
        for(Post post : posts){
            author.add(post.getAuthor());
        }
        model.addAttribute("authorsList", author);
        model.addAttribute("filter", filter);
        model.addAttribute("listOfPosts", posts);

        return "index";
    }
    @GetMapping("/filterByTag")
    public String filterByTag(@RequestParam("tags") String tags, Model model){
        Tag tag = tagService.findTagsByName(tags);
        List<Post> posts = tag.getPosts();
        model.addAttribute("listOfPosts", posts);
        return "index";
    }

    @GetMapping("/filterByAuthor")
    public String filterByAuthor(Model model, @RequestParam("author") String[] author){
        List<Post> posts = author == null ? postService.findAllPosts() : postService.filterByAuthor(author);
        model.addAttribute("listOfPosts", posts);
        return "index";
    }

    @PostMapping("/savePost")
    public String savePost(@RequestParam String tag, @ModelAttribute("post") Post posts, Model model){
        posts.setPublishedAt(LocalDate.now());
        posts.setCreatedAt(LocalDate.now());
        posts.setUpdatedAt(LocalDate.now());
        posts.setExcerpt(posts.getContent().substring(0,10));

        String[] listOfTag = tag.split(" ");
        Tag tags = new Tag();
        for(String tagName : listOfTag){
            if(tagService.checkTagByName(tagName)){
                tags = tagService.findTagsByName(tagName);
            }else{
                tags.setName(tagName);
                tags.setCreatedAt(LocalDate.now());
            }
            if(posts.getTags()==null){
                posts.addTag(tags);
            }else if((posts.getTags()!=null)&& !(posts.getTags().contains(tags))){
                posts.addTag(tags);
            }
        }
        postService.savePost(posts);
        return "redirect:/";
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String showFormForUpdate(@PathVariable (value = "id") long id, Model model){
        Post posts = postService.getPostById(id);
        model.addAttribute("posts", posts);
        return "update_post";
    }

    @GetMapping("/showPost/{id}")
    public String showPost(@PathVariable (value = "id") long id, @ModelAttribute Comment comments, Model model){
        Post posts = postService.getPostById(id);
        model.addAttribute("posts", posts);
        List<Comment> commentsList = posts.getComments();
        model.addAttribute("commentList", commentsList);
        return "view";
    }
    @GetMapping("/deletePost/{id}")
    public String deletePost(@PathVariable (value = "id") long id){
        this.postService.deletePostById(id);
        return "redirect:/";
    }

    @GetMapping("/page/{pageNo}")
    public String findPaginated(@PathVariable (value = "pageNo") int pageNo,
                                @RequestParam("sortPost") String sortPost,
                                @RequestParam("sortDir") String sortDir,
                                Model model){
        int pageSize=5;
        Page<Post> page = postService.findPaginated(pageNo,pageSize, sortPost, sortDir);
        List<Post> listOfPosts = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalPages());

        model.addAttribute("sortPost", sortPost);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        model.addAttribute("listOfPosts", listOfPosts);

        return "index";
    }
}
