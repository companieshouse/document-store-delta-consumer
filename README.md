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

## Docker
1. Remove any enabled modules in Docker using:\
```
chs-dev status
chs-dev modules disable <module_you_have_currently_enabled>
```
2. Enable the ‘delta’ module in Docker
```
chs-dev modules enable delta
```

3. (Optional) If you want to make changes to document-store-delta-consumer, clone this git repository and enable development mode in docker with
```
chs-dev development enable document-store-delta-consumer
```

4. Start Docker
```
chs-dev up
```

5. Open your MongoDB app and connect to the Docker DB

6. Open the ```company-filing-history/company-filing-history``` collection

7. Find the record with the following id: ```_id: "MzA0Mzk3MjY3NWFkaXF6a2N4"```\
It should have :
```company_number: "12345678"```

8. Expand the record's fields and delete the ```data.links.document_metadata``` field if it has one.\
This will get re-added if the Postman post request is successful.

9. Make a note of the ```version``` value on this record, it should increase by one when you submit via Postman.

10. Open Postman and open/log into the Team Workspace.

11. Open the folder “Data Sync/Document Store” and there should be a Post request already saved there, open it.

12. It should have a body that looks like :
```
{
  "category": "officers",
  "significant_date": "2014-09-24'T'00:00:00.000Z",
  "significant_date_type": "made-up-date",
  "barcode": "XHJYVXAY",
  "company_number": "12345678",
  "stored_image_url": "s3://document-api-images-cidev/9616659692.tiff",
  "transaction_id": "3043972675",
  "document_type": "TM01",
  "filename": "9616659692.tiff",
  "page_count": 3
}
```
and a URL:
```
{{API_GATEWAY}}/delta/document-store
```

13. To use it, select the correct environment in top right of Postman window, select CHS Docker (incomplete) and press the 'send' button.

14. You should see a 200 OK response in Postman

15. Check the ```document-store-delta-consumer``` logs in Docker, you should see no errors, and you should see entries like:
```
trace: Mapping delta [class DocumentStoreDelta
trace:  Mapping valid page count [3]
trace: Mapped delta to CreateDocumentApi [CreateDocumentApi{transactionId='CHIPS:3043972675', companyNumber='12345678', significantDate='2014-09-24T00:00:00.00Z', significantDateType='made-up-date', barcode=XHJYVXAY, category='officers', pages='3', filename='9616659692.tiff', storedImageUrl='s3://document-api-images-cidev/9616659692.tiff'}]
info: Updating filing history document metadata
info: Updated filing history document metadata successfully
info: Processed delta
```
16. Check the record in Mongo DB again, it should now have the ```document_metadata``` link added and the ```version number``` should increase by one.