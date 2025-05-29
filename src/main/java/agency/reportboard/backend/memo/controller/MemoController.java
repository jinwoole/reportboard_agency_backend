package agency.reportboard.backend.memo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import agency.reportboard.backend.memo.domain.Memo;
import agency.reportboard.backend.memo.service.MemoService;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class MemoController {
    @Autowired
    private MemoService memoService;

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello, Memo!";
    }

    @GetMapping("/api/memos")
    public List<Memo> getMemos() {
        return memoService.getMemos();
    }
}
