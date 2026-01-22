# document-store-delta-consumer

Kafka consumer that processes document store deltas from CHIPS into CHS. Consumes from the `document-store-delta` topic, transforms messages, and sends requests to the Document API and Filing History Document Metadata API.

## Quickstart

### 1. Authenticate with ECR

```bash
aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin 169942020521.dkr.ecr.eu-west-2.amazonaws.com
aws ecr get-login-password --region eu-west-2 | docker login --username AWS --password-stdin 416670754337.dkr.ecr.eu-west-2.amazonaws.com
```

### 2. Enable and Start Services

Navigate to the docker-chs-development on your machine

```bash
chs-dev modules enable delta
chs-dev up
```

### 3. Verify Services

```bash
docker ps | grep -E "chs-delta-api|document-store-delta-consumer|kafka"
```
Or

```bash
chs-dev status
```

## Testing with Postman

**Endpoint:**
```
POST http://api.chs.local:4001/delta/document-store
```

**Headers:**
| Header | Value |
|--------|-------|
| `Content-Type` | `application/json` |
| `ERIC-Access-Token` | `<your-api-key>` |

**Request Body:**
```json
{
  "category": "accounts",
  "transaction_id": "1234567890",
  "company_number": "00006400",
  "stored_image_url": "http://example.com/image.tiff"
}
```

**Required fields:** `category`, `transaction_id`, `company_number`, `stored_image_url`

**Optional fields:** `barcode`, `significant_date`, `significant_date_type`, `parent_transaction_id`, `document_type`, `parent_document_type`, `page_count`, `filename`, `delta_at`

**Category values:** `accounts`, `registered-office-change`, `officers`, `annual-returns`, `new-companies`, `miscellaneous`, `capital`, `liquidations`, `changes-of-name`, `constitutional`, `mortgages`

### View API Spec

```bash
docker exec chs-delta-api cat /opt/apispec/document-store-delta-spec.yml
```

## Troubleshooting

**500 Error - Kafka not reachable:**
```bash
docker start docker-chs-development-kafka-1
```

**403 Error - ECR auth expired:** Re-run the ECR login commands above.

**View logs:**
```bash
docker logs document-store-delta-consumer -f
docker logs chs-delta-api -f
```

## Building

```bash
make clean build    # Build
make test           # Run all tests
```

## Configuration

Key environment variables (see `docker-compose.yaml` for full list):

| Variable | Default | Description |
|----------|---------|-------------|
| `BOOTSTRAP_SERVER_URL` | `kafka:9092` | Kafka broker |
| `DOCUMENT_STORE_DELTA_TOPIC` | `document-store-delta` | Kafka topic |
| `API_KEY` | - | Internal API key |
| `DOCUMENT_API_URL` | - | Document API URL |
