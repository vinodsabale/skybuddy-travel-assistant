package com.ai_assistence_service.travelassient.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestClient;

import com.ai_assistence_service.travelassient.tool.FlightTools;
import com.ai_assistence_service.travelassient.tool.HotelTools;
import com.ai_assistence_service.travelassient.tool.PolicyTools;
import com.ai_assistence_service.travelassient.tool.WeatherTools;

@Configuration
public class AiConfig {

    private static final int MAX_MEMORY_MESSAGES = 20;

    private static final String POLICY_SYSTEM_PROMPT = """
            You answer questions about airline and travel policies, terms and conditions.
            Use only the provided context. If the answer is not in the context, say that the
            information is not available in the uploaded documents.
            """;

    private static final String INTENT_SYSTEM_PROMPT = """
            You extract the travel intent from the user message.
            Return the intent type, origin city, destination city, travel date in yyyy-MM-dd format
            and the number of passengers. Use null for anything the user did not mention.
            """;

    // ------------------------------------------------------------------ REST clients

    /**
     * Default builder. Spring AI uses it to talk to Groq,
     * so it must NOT be load balanced.
     */
    @Bean
    @Primary
    @Scope("prototype")
    public RestClient.Builder defaultRestClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Load balanced builder. Resolves service names such as http://flight-service through Eureka.
     */
    @Bean
    @LoadBalanced
    @Scope("prototype")
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // ------------------------------------------------------------------ Chat memory

    /** Chat history is kept in memory (no database needed). It is cleared when the app restarts. */
    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(MAX_MEMORY_MESSAGES)
                .build();
    }

    // ------------------------------------------------------------------ Vector store

    /**
     * Vector store without any database: vectors live in memory and are saved to a JSON file,
     * so uploaded documents are still there after a restart.
     */
    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel,
                                         @Value("${travel.rag.store-file}") String storeFile) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(storeFile);
        if (file.exists() && file.length() > 0) {
            store.load(file);
        }
        return store;
    }

    // ------------------------------------------------------------------ Chat clients

    /**
     * Main agent: chat memory + tool calling (flights, hotels, weather, policy documents).
     */
    @Bean
    public ChatClient travelAssistantClient(ChatModel chatModel,
                                            ChatMemory chatMemory,
                                            FlightTools flightTools,
                                            HotelTools hotelTools,
                                            WeatherTools weatherTools,
                                            PolicyTools policyTools) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(flightTools, hotelTools, weatherTools, policyTools)
                .build();
    }

    /**
     * Classic RAG: QuestionAnswerAdvisor retrieves chunks from the vector store and adds them to the prompt.
     */
    @Bean
    public ChatClient policyRagClient(ChatModel chatModel,
                                      VectorStore vectorStore,
                                      @Value("${travel.rag.top-k}") int topK,
                                      @Value("${travel.rag.similarity-threshold}") double similarityThreshold) {
        return ChatClient.builder(chatModel)
                .defaultSystem(POLICY_SYSTEM_PROMPT)
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .topK(topK)
                                .similarityThreshold(similarityThreshold)
                                .build())
                        .build())
                .build();
    }

    /**
     * Structured output: converts free text into a Java object.
     */
    @Bean
    public ChatClient intentExtractionClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(INTENT_SYSTEM_PROMPT)
                .build();
    }
}
