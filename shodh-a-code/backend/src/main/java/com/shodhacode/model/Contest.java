package com.shodhacode.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "contest")
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ Auto-generate IDs
    private Long id;

    private String name;

    @OneToMany(
        mappedBy = "contest",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @JsonManagedReference // 🔑 Pairs with @JsonBackReference in Problem
    private List<Problem> problems;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Problem> getProblems() { return problems; }
    public void setProblems(List<Problem> problems) { this.problems = problems; }
}
