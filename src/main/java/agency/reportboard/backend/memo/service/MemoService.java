package agency.reportboard.backend.memo.service;

import agency.reportboard.backend.memo.domain.Memo;
import agency.reportboard.backend.memo.repository.MemoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoService {
    @Autowired
    private MemoRepository memoRepository;

    public List<Memo> getMemos() {
        return memoRepository.findAll();
    }
}
