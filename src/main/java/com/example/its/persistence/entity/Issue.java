package com.example.its.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Integer issueId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_account_id", nullable = false)
    private Account reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_account_id")
    private Account assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixer_account_id")
    private Account fixer;

    @Column(name = "reported_at", nullable = false)
    private String reportedAt;

    @Column(name = "last_modified_at", nullable = false)
    private String lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "issue", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "issue", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IssueHistory> issueHistories = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "issue_tags",
        joinColumns = @JoinColumn(name = "issue_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    protected Issue() {
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = IssueStatus.NEW;
        }
        if (priority == null) {
            priority = Priority.MAJOR;
        }
        if (reportedAt == null) {
            reportedAt = LocalDateTime.now().toString();
        }
        if (lastModifiedAt == null) {
            lastModifiedAt = reportedAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now().toString();
    }

    public Long getIssueId() {
        return issueId != null ? issueId.longValue() : null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Account getReporter() {
        return reporter;
    }

    public void setReporter(Account reporter) {
        this.reporter = reporter;
    }

    public Account getAssignee() {
        return assignee;
    }

    public void setAssignee(Account assignee) {
        this.assignee = assignee;
    }

    public Account getFixer() {
        return fixer;
    }

    public void setFixer(Account fixer) {
        this.fixer = fixer;
    }

    public LocalDateTime getReportedAt() {
        return parseDateTime(reportedAt);
    }

    public LocalDateTime getLastModifiedAt() {
        return parseDateTime(lastModifiedAt);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<IssueHistory> getIssueHistories() {
        return issueHistories;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setIssue(this);
    }

    public void addIssueHistory(IssueHistory history) {
        issueHistories.add(history);
        history.setIssue(this);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getIssues().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getIssues().remove(this);
    }

    // BE: static factory method 컨벤션 만족 위해 추가
    public static Issue create(String title, String description, Priority priority, Project project, Account reporter) {
        Issue issue = new Issue();
        issue.title = title;
        issue.description = description;
        issue.priority = priority != null ? priority : Priority.MAJOR;
        issue.project = project;
        issue.reporter = reporter;
        issue.status = IssueStatus.NEW; // 초기 상태 강제
        return issue;
    }

    private static LocalDateTime parseDateTime(String value) {
        return value != null ? LocalDateTime.parse(value.replace(' ', 'T')) : null;
    }
}
