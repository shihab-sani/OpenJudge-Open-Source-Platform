package open.judge.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import open.judge.DTOs.CreateSubmissionRequestDto;
import open.judge.DTOs.SubmissionResponseDto;
import open.judge.Services.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Submit code for evaluation")
public class SubmissionController {
    private final SubmissionService submissionService;

    @PostMapping
    @Operation(summary = "Create a submission (enqueues asynchronous evaluation)")
    public ResponseEntity<SubmissionResponseDto> submit(
            @Valid @RequestBody CreateSubmissionRequestDto request) {
        SubmissionResponseDto response = SubmissionResponseDto.from(submissionService.createSubmission(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get submission status and verdict")
    public SubmissionResponseDto get(@PathVariable Long id) {
        return SubmissionResponseDto.from(submissionService.getSubmission(id));
    }
}
