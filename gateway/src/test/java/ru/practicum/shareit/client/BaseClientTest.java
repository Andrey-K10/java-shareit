package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BaseClientTest {

    private RestTemplate restTemplate;
    private TestClient testClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        testClient = new TestClient(restTemplate);
    }

    @Test
    void get_whenNoParameters_shouldCallRestTemplate() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.getPublic("/test");

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void get_whenWithUserId_shouldCallRestTemplate() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.get("/test", 1L);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void get_whenWithParameters_shouldCallRestTemplate() {
        Map<String, Object> params = Map.of("key", "value");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class), anyMap()))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.get("/test", 1L, params);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class), eq(params));
    }

    @Test
    void post_whenWithBody_shouldCallRestTemplate() {
        String body = "test body";
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.post("/test", body);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.POST), any(), eq(Object.class));
    }

    @Test
    void post_whenWithUserId_shouldCallRestTemplate() {
        String body = "test body";
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.post("/test", 1L, body);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.POST), any(), eq(Object.class));
    }

    @Test
    void patch_whenWithBody_shouldCallRestTemplate() {
        String body = "test body";
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.patch("/test", body);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.PATCH), any(), eq(Object.class));
    }

    @Test
    void patch_whenWithUserId_shouldCallRestTemplate() {
        String body = "test body";
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.patch("/test", 1L, body);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.PATCH), any(), eq(Object.class));
    }

    @Test
    void put_whenWithBody_shouldCallRestTemplate() {
        String body = "test body";
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.put("/test", 1L, body);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.PUT), any(), eq(Object.class));
    }

    @Test
    void delete_whenWithUserId_shouldCallRestTemplate() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> result = testClient.delete("/test", 1L);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(restTemplate).exchange(eq("/test"), eq(HttpMethod.DELETE), any(), eq(Object.class));
    }

    @Test
    void makeAndSendRequest_whenHttpStatusCodeException_shouldReturnErrorResponse() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("error".getBytes());
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> result = testClient.get("/test", 1L);

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertArrayEquals("error".getBytes(), (byte[]) result.getBody());
    }

    // Test implementation of BaseClient
    private static class TestClient extends BaseClient {
        public TestClient(RestTemplate rest) {
            super(rest);
        }

        public ResponseEntity<Object> getPublic(String path) {
            return get(path);
        }

        @Override
        public ResponseEntity<Object> get(String path, Long userId, Map<String, Object> parameters) {
            return super.get(path, userId, parameters);
        }

        @Override
        public <T> ResponseEntity<Object> post(String path, Long userId, T body) {
            return super.post(path, userId, body);
        }

        @Override
        public <T> ResponseEntity<Object> patch(String path, T body) {
            return super.patch(path, body);
        }

    }
}