# Cloud Assess

![Repository Status](https://www.repostatus.org/badges/latest/active.svg)
![Tests](https://github.com/kleis-technology/cloud-assess/actions/workflows/test.yaml/badge.svg)

This is the official repository of [Cloud Assess](https://cloudassess.org).

## What is Cloud Assess?

Cloud Assess is an open-source tool to automate the assessment of
the environmental impacts of cloud services.

![Cloud Assess](./assets/cloudassess.svg)

## Table of Contents

1. [Getting Started](#getting-started)
2. [First Assessment](#first-assessment)
3. [How does it work?](#how-does-it-work)
    * [Trusted Library](#trusted-library)
    * [Adapt the models to your taste](#adapt-the-models-to-your-taste)
4. [License](#license)
5. [About us](#about-us)

## Getting Started

First, you need to clone this repository:

```bash
git checkout git@github.com:kleis-technology/cloud-assess.git
cd cloud-assess
```

All the commands in the sections below are to be run from the root of this repository.
We assume that you will run these commands in bash.
If you are using another shell, please adapt the commands accordingly.

### Docker-compose

You can run the server using `docker-compose`.

```bash
docker compose up -d
```

The API specification is available in the folder `openapi`.
For a more interactive visualisation of the API, the `docker-compose`
also spins up a swagger ui instance.
Visit this [page](http://localhost) to explore the endpoints, DTOs and run example queries.

### Local build and run

#### Requirements

To build the server, you will need

* Java 17 (temurin) environment
*

A [GitHub personal access token (classic)](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic)
with the permission `read:packages` to download packages from GitHub Package Registry.

Then, set up the following environment variables.

```bash
export GITHUB_ACTOR=<your GitHub username>
export GITHUB_TOKEN=<the token you just created>
```

#### Procedure

From the root of the git repository, run

```bash
./gradlew build
```

To run the server locally

```bash
./gradlew bootRun
```

The server should start listening for requests on `localhost:8080`.

Note that, if you are on Windows, use the command `./gradlew.bat` instead of `./gradlew`.

## First Assessment

Each functional unit is associated with a specific API endpoint.
For instance, say we want to assess the environmental impact of using a virtual machine.
The request takes the following form:

```json
{
  "period": {
    "amount": 1.0,
    "unit": "hour"
  },
  "virtual_machines": [
    {
      "id": "c1",
      "pool_id": "client_vm",
      "vcpu": {
        "amount": 1.0,
        "unit": "vCPU"
      },
      "ram": {
        "amount": 32.0,
        "unit": "GB"
      },
      "storage": {
        "amount": 1024.0,
        "unit": "GB"
      },
      "meta": {
        "region": "sofia",
        "env": "production"
      }
    }
  ]
}
```

In this request, we specify the usage of our virtual machine in terms
of the quantity of resources used (ram, storage and vCPU).

Running this request yields an impact assessment with the common LCA indicators.
Of course, in this example, we ran the assessment for a single virtual machine,
but nothing stops you from assessing as many virtual machines as you want.
<details>
<summary>
    Expand to see what the result looks like.
</summary>

```json
{
  "virtual_machines": [
    {
      "request": {
        "id": "c1",
        "quantity": {
          "amount": 1.0,
          "unit": "hour"
        },
        "meta": {
          "region": "sofia",
          "env": "production"
        }
      },
      "impacts": {
        "ADPe": {
          "total": {
            "amount": 1.2442674861138006E-7,
            "unit": "kg Sb-Eq"
          }
        },
        "ADPf": {
          "total": {
            "amount": 0.5856626163896165,
            "unit": "MJ, net calorific value"
          }
        },
        "AP": {
          "total": {
            "amount": 2.4412031648686188E-4,
            "unit": "mol H+-Eq"
          }
        },
        "GWP": {
          "total": {
            "amount": 0.04522166507916957,
            "unit": "kg CO2-Eq"
          }
        },
        "LU": {
          "total": {
            "amount": 0.004520589981659305,
            "unit": "u"
          }
        },
        "ODP": {
          "total": {
            "amount": 0.06362503350653016,
            "unit": "kg CFC-11-Eq"
          }
        },
        "PM": {
          "total": {
            "amount": 6.97494882258087E-9,
            "unit": "disease incidence"
          }
        },
        "POCP": {
          "total": {
            "amount": 1.6048978567682187E-9,
            "unit": "kg NMVOC-Eq"
          }
        },
        "WU": {
          "total": {
            "amount": 1.1112742040640779E-4,
            "unit": "m3 world eq. deprived"
          }
        },
        "CTUe": {
          "total": {
            "amount": 0.789792211082212,
            "unit": "CTUe"
          }
        },
        "CTUh_c": {
          "total": {
            "amount": 9.6110170405875E-12,
            "unit": "CTUh"
          }
        },
        "CTUh_nc": {
          "total": {
            "amount": 3.969223225207655E-10,
            "unit": "CTUh"
          }
        },
        "Epf": {
          "total": {
            "amount": 1.497996507152073E-5,
            "unit": "kg P-Eq"
          }
        },
        "Epm": {
          "total": {
            "amount": 3.918786733484802E-5,
            "unit": "kg N-Eq"
          }
        },
        "Ept": {
          "total": {
            "amount": 3.99503638233262E-4,
            "unit": "mol N-Eq"
          }
        },
        "IR": {
          "total": {
            "amount": 6.123032983267816E-5,
            "unit": "kBq U235-Eq"
          }
        }
      }
    }
  ]
}
```

</details>

## How does it work?

### Trusted Library

The ambition of Cloud Assess is to offer a library of *transparent*,
*PCR-compliant* and *executable* LCA models in the sector of digital services.
More precisely, this work builds on the
new PCR (to be published soon) for data center and cloud services.
The PCR defines 7 functional units, covering the hosting infrastructure (physical datacenter) up to
more abstract ones, e.g., virtual machines. 
Cloud Assess aims at covering all functional units, but also FU that are not explicitly covered by the PCR

| PCR No. | Functional Unit    | Model | API endpoint |
|---------|--------------------|-------|--------------|
| 1       | Datacenter         | n/a   | n/a          |
| 2       | Physical server    | ✅     | planned      |
| 3       | Storage            | ✅     | planned      |
| 4       | Network equipment  | ✅     | planned      |
| 5       | Computing resource | ✅     | planned      |
| 6       | Virtual machine    | ✅     | ✅            |
| 7       | Block storage      | ✅     | planned      |

These models are specified under the folder `trusted_library`.

The library also contains intermediate/generic models, e.g.,

| Functional unit            | Model |
|----------------------------|-------|
| Pool of servers            | ✅     |
| Pool of storage equipments | ✅     |
| Pool of network equipments | ✅     |

which can be used for specific purposes, e.g., a mail service running on a dedicated infrastructure.

### Adapt the models to your taste

#### Structure

The models are written in the [LCA as CODE](https://lca-as-code.com) language.
This is a domain-specific language designed for the need of lifecycle analysis.
The models under the folder `trusted_library` are directly loaded in the server
to perform the assessments.
A [tutorial](https://lca-as-code.com/book) is available if you want to learn
more about the language itself.
The source repository is available [here](https://github.com/kleis-technology/lcaac).

The folder `trusted_library` is organized as follows.

* `01-hardware` : this folder contains the PCR models for the functional units 1 to 4.
* `02-pooling` : this folder contains the PCR models for the functional units 5 to 8.
* `03-services` : this folder contains the PCR models for functional units 9 and 10.

Here is an illustration of this layering structure.

<img alt="sankey" src="assets/sankey.png" width=800/>

#### Configuring the models

You can read and edit the models in `trusted_library` with any text editor.
Unless you need to customize one of the functional units, there is no need to actually modify the models.

The folder `trusted_library/data` contains inventory files (in CSV format),
and associated emission factors. To adapt Cloud Assess you must fill in, at least,
the following inventories

* `data/01-hardware/dc_inventory.csv` : this file lists the available datacenters.
* `data/01-hardware/hw_inventory.csv` : this file lists the available hardware equipments.

The following files should be filled with the relevant emission factors:

* `data/01-hardware/dc_impacts.csv`
* `data/01-hardware/hw_impacts.csv`
* `data/01-hardware/electricity_mix.csv`

Note that the emission factors presented in this repository are mock values.

## License

The content of this repository is subject to [Apache 2 license](./LICENSE-2.0.txt).

## About us

[Cloud Assess](https://cloudassess.org) is a joint-venture of [Resilio](https://resilio-solutions.com)
and [Kleis Technology](https://kleis.ch).

If you have any questions related to Cloud Assess, be it about the LCA methodology or ways to automate the assessments,
feel free to reach out to us at `contact@resilio.tech`.

<span>
<img alt="Resilio" src="assets/resilio.svg" style="background: white; padding: 2px"/> & <img alt="Kleis" src="assets/kleis.svg" style="background: white; padding: 2px"/>
</span>


