package com.iblochko.notes.controller;

import com.iblochko.notes.dto.TagDto;
import com.iblochko.notes.exception.ErrorResponse;
import com.iblochko.notes.model.Tag;
import com.iblochko.notes.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/tags")
@AllArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "API for managing tags")
public class TagsController {
    private final TagService tagService;

    @GetMapping("/all")
    @Operation(summary = "Get all tags",
            description =
                    "Returns a list of all tags")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tags"),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<Tag>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by id",
            description =
                    "Returns a tag with entered id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tag"),
        @ApiResponse(responseCode = "404", description = "Tag not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Post new tag",
            description =
                    "Create new tag and save it to database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tag"),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TagDto> createTag(@RequestBody TagDto tagDto) {
        TagDto tag = tagService.createTag(tagDto);
        return new ResponseEntity<>(tag, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Put tag",
            description =
                    "Update tag and save changes to database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tag"),
        @ApiResponse(responseCode = "400", description = "Bad request",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Resource not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TagDto> updateTag(@PathVariable Long id, @RequestBody TagDto tagDto) {
        TagDto tag = tagService.updateTag(id, tagDto);
        return new ResponseEntity<>(tag, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tag",
            description =
                    "Delete tag from database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted tag"),
        @ApiResponse(responseCode = "404", description = "Tag not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteTag(@Parameter(description = "Tag id", required = true)
                                          @PathVariable Long id) {
        tagService.deleteTag(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}