package com.gamegoo.gamegoo_v2.core.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Properties;

import static java.lang.System.exit;

@Slf4j
@Profile("local") // profile이 local일 때만 빈 생성
@Component
@ConfigurationProperties(prefix = "ssh") // application.yml의 ssh 영역에서 환경변수 가져오기
@Validated // 필드 유효성 검사
@Setter
public class SshTunnelingInitializer {

    @NotNull
    private String remoteJumpHost;
    @NotNull
    private String user;
    @NotNull
    private int sshPort;
    @NotNull
    private String privateKey;
    @NotNull
    private String databaseUrl;
    @NotNull
    private int databasePort;

    private Session session;

    @PreDestroy // 빈이 삭제될 때 호출
    public void closeSSH() {
        if (session.isConnected()) {
            session.disconnect(); // ssh 연결 종료
        }
    }

    /**
     * ssh 터널링 생성 및 포트 반환
     *
     * @return
     */
    public Integer buildSshConnection() {
        Integer forwardedPort = null;

        try {
            log.info("{}@{}:{}:{} with privateKey", user, remoteJumpHost, sshPort, databasePort);

            log.info("start ssh tunneling..");
            JSch jSch = new JSch();

            log.info("creating ssh session");
            jSch.addIdentity(privateKey);  // private key 파일 추가
            session = jSch.getSession(user, remoteJumpHost, sshPort);  // ssh 세션 설정

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no"); // 서버 호스트 키 검증 비활성화
            session.setConfig(config);
            log.info("complete creating ssh session");

            log.info("start connecting ssh connection");
            session.connect();  // ssh 연결
            log.info("success connecting ssh connection ");

            // 로컬pc의 임의의 포트와 원격 접속한 pc의 db포트 연결
            log.info("start forwarding");
            forwardedPort = session.setPortForwardingL(0, databaseUrl, databasePort);
            log.info("successfully connected to database");

        } catch (Exception e) {
            log.error("fail to make ssh tunneling");
            this.closeSSH(); // ssh 연결 해제
            e.printStackTrace();
            exit(1);
        }

        return forwardedPort; // 포워딩된 포트 번호 반환
    }

}

