package agency.reportboard.backend.user.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_credentials")
public class UserCredential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String credentialId;
    
    @Lob
    @Column(nullable = false)
    private byte[] publicKey;
    
    @Column(nullable = false)
    private long signCount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // 기본 생성자
    public UserCredential() {}
    
    public UserCredential(String credentialId, byte[] publicKey, long signCount, User user) {
        this.credentialId = credentialId;
        this.publicKey = publicKey;
        this.signCount = signCount;
        this.user = user;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCredentialId() {
        return credentialId;
    }
    
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }
    
    public byte[] getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }
    
    public long getSignCount() {
        return signCount;
    }
    
    public void setSignCount(long signCount) {
        this.signCount = signCount;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}
