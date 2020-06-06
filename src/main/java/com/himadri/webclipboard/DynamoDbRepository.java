package com.himadri.webclipboard;

import com.himadri.webclipboard.entity.Clipboard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class DynamoDbRepository {
    public static final String TABLE_NAME = "WebClipboard";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_ENCRYPTED = "encrypted";
    public static final String COLUMN_DATE = "date";

    @Value("${accessKeyId}")
    private String accessKeyId;

    @Value("${secretAccessKey}")
    private String secretAccessKey;

    private DynamoDbClient dynamoDbClient;

    @PostConstruct
    public void init() {
        dynamoDbClient = DynamoDbClient.builder()
            .credentialsProvider(() -> AwsBasicCredentials.create(accessKeyId, secretAccessKey))
            .region(Region.EU_CENTRAL_1)
            .build();
    }

    public void save(Clipboard clipboard) {
        dynamoDbClient.putItem(PutItemRequest.builder()
            .tableName(TABLE_NAME)
            .item(Map.of(
                COLUMN_USER_ID, AttributeValue.builder().s(clipboard.getUser()).build(),
                COLUMN_ENCRYPTED, AttributeValue.builder().b(SdkBytes.fromByteArray(clipboard.getEncrypted())).build(),
                COLUMN_DATE, AttributeValue.builder().n(Long.toString(clipboard.getDate())).build()
            ))
            .build());
    }

    public byte[] getEncryptedText(String user) {
        GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(Map.of(COLUMN_USER_ID, AttributeValue.builder().s(user).build()))
            .build());
        if (getItemResponse == null ||
            getItemResponse.item() == null ||
            !getItemResponse.item().containsKey(COLUMN_ENCRYPTED)
        ) {
            return null;
        }
        return getItemResponse.item().get(COLUMN_ENCRYPTED).b().asByteArray();
    }

}
