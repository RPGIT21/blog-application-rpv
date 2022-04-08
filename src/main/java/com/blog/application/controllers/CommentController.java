package com.blog.application.controllers;

import com.blog.application.models.Comment;
import com.blog.application.services.CommentService;
import com.blog.application.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @GetMapping("/saveComment/{id}")
    public String saveComment(@PathVariable(value = "id") long id, @ModelAttribute("comments") Comment comments, Model model){
        comments.setCreatedAt(LocalDate.now());
        comments.setUpdatedAt(LocalDate.now());
        //model.addAttribute("comments", comments);
        commentService.saveComment(id, comments);
        postService.saveComment(id, comments);
        return "redirect:/showPost/" + id;
    }

    @GetMapping("/deleteComment/{id}")
    public String deletePost(@PathVariable (value = "id") long id){
        this.commentService.deleteCommentById(id);
        return "redirect:/showPost/" + id;
    }

    @GetMapping("/showFormForUpdateComment/{id}")
    public String showFormForUpdate(@PathVariable (value = "id") long id, Model model){
        Comment comments = commentService.getCommentById(id);
        model.addAttribute("comments", comments);

        return "update_comment";
    }
}
