package com.ai_assistence_service.travelassient.service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ai_assistence_service.travelassient.dto.DocumentUploadResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * ETL pipeline: Extract (Tika reader) -> Transform (token splitter) -> Load (embed and store in the vector store file).
 */
@Service
@Slf4j
public class DocumentIngestionService {

    private final SimpleVectorStore vectorStore;
    private final File storeFile;

    public DocumentIngestionService(SimpleVectorStore vectorStore,
                                    @Value("${travel.rag.store-file}") String storeFile) {
        this.vectorStore = vectorStore;
        this.storeFile = new File(storeFile);
    }

    public synchronized DocumentUploadResponse ingest(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a non-empty file");
        }

        String fileName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();

        // Extract
        List<Document> documents = new TikaDocumentReader(toResource(file, fileName)).get();
        documents.forEach(document -> document.getMetadata().put("source", fileName));

        // Transform
        List<Document> chunks = new TokenTextSplitter().apply(documents);
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("No readable text found in " + fileName);
        }

        // Load (the vector store creates the embeddings through the embedding model)
        vectorStore.add(chunks);
        saveStore();
        log.info("Stored {} chunks from {}", chunks.size(), fileName);

        return new DocumentUploadResponse(fileName, chunks.size());
    }

    private void saveStore() {
        File parent = storeFile.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        vectorStore.save(storeFile);
    }

    private Resource toResource(MultipartFile file, String fileName) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not read the uploaded file", ex);
        }
    }
}
