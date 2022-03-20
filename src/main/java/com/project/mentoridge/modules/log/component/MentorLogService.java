package com.project.mentoridge.modules.log.component;

import com.project.mentoridge.modules.account.vo.Mentor;
import com.project.mentoridge.modules.log.repository.LogRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.util.function.Function;

@Service
public class MentorLogService extends LogService<Mentor> {

    public MentorLogService(LogRepository logRepository) {
        super(logRepository);
    }

    @PostConstruct
    void init() {
        properties.add(new Property("user", "사용자"));
        properties.add(new Property("bio", "소개"));

        Function<Mentor, String> func = mentor -> mentor.getUser().getUsername();
        functions.put("user", func);
    }

    @Override
    protected void insert(PrintWriter pw, Mentor vo) throws NoSuchFieldException, IllegalAccessException {

        pw.print("[Mentor] ");
        printInsertLogContent(pw, vo, properties, functions);
    }

    @Override
    protected void update(PrintWriter pw, Mentor before, Mentor after) throws NoSuchFieldException, IllegalAccessException {

        pw.print("[Mentor] ");
        printUpdateLogContent(pw, before, after, properties, functions);
    }

    @Override
    protected void delete(PrintWriter pw, Mentor vo) throws NoSuchFieldException, IllegalAccessException {

        pw.print("[Mentor] ");
        printDeleteLogContent(pw, vo, properties, functions);
    }
}
