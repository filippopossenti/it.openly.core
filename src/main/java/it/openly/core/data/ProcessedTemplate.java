package it.openly.core.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ProcessedTemplate {
    String sql;
    Map<String, Object> context;
}
