package org.example.backend.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SequenceDocument {

    @Id
    private String sequenceName;

    private long value;
}

