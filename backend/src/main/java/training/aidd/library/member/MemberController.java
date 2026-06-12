package training.aidd.library.member;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> listMembers() {
        return memberService.findAll();
    }

    @GetMapping("/{id}")
    public MemberResponse getMember(@PathVariable Long id) {
        return memberService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@RequestBody MemberRequest request) {
        return memberService.create(request);
    }

    @PutMapping("/{id}")
    public MemberResponse updateMember(@PathVariable Long id, @RequestBody MemberRequest request) {
        return memberService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateMember(@PathVariable Long id) {
        memberService.deactivate(id);
    }
}
