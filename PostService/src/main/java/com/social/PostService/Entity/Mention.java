package com.social.PostService.Entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mentions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mention {

    @Id
    @GeneratedValue
    private UUID id;

    private String username;
}
