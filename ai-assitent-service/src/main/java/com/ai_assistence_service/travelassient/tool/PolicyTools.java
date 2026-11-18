package com.ai_assistence_service.travelassient.tool;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Agentic RAG: the model decides when it needs the uploaded documents and this tool
 * fetches the most similar chunks from the vector store.
 */
@Component
public class PolicyTools {

    private static final String NOTHING_FOUND = "No relevant information found in the uploaded policy documents.";

    private final VectorStore vectorStore;
    private final int topK;
    private final double similarityThreshold;

    public PolicyTools(VectorStore vectorStore,
                       @Value("${travel.rag.top-k}") int topK,
                       @Value("${travel.rag.similarity-threshold}") double similarityThreshold) {
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    @Tool(description = "Search the uploaded company documents (terms and conditions, baggage rules, refund and "
            + "cancellation policy, travel rules) and return the most relevant passages. "
            + "Use it for every policy or rules question.")
    public List<String> searchPolicyDocuments(
            @ToolParam(description = "The policy question to look up, in plain words") String question) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build());

        if (documents == null || documents.isEmpty()) {
            return List.of(NOTHING_FOUND);
        }
        return documents.stream().map(Document::getText).toList();
    }
}
