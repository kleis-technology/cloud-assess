# Cloud Assess

This is the official repository for Cloud Assess.

## LCA Models

The LCA models are presented under the folder `models`.
They are written in the `LCA as Code` language, and are directly loaded in the server to perform the assessments.

## Run locally

The server is available as a docker image.
```bash
docker run -p 8080:8080 -v models:/models ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:<version> 
```

Test the server
```bash
curl -X POST localhost:8080/virtual_machines\
  -H "Content-Type: application/json"\
  -d @- << EOF
{
  "internal_workload": {
    "ram": {
      "amount": 10.0,
      "unit": "GB_hour"
    },
    "storage": {
      "amount": 50.0,
      "unit": "GB_hour"
    }
  },
  "virtual_machines": [
    {
      "id": "c1",
      "ram": {
        "amount": 32.0,
        "unit": "GB_hour"
      },
      "storage": {
        "amount": 1024.0,
        "unit": "GB_hour"
      },
      "meta": {
        "region": "sofia",
        "env": "production"
      }
    },
    {
      "id": "c2",
      "ram": {
        "amount": 16.0,
        "unit": "GB_hour"
      },
      "storage": {
        "amount": 2048.0,
        "unit": "GB_hour"
      },
      "meta": {
        "region": "sofia",
        "env": "production"
      }
    }
  ]
}
EOF
```
