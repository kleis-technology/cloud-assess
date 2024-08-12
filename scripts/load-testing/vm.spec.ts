import { VirtualMachineListDto, VirtualMachineListDtoToJSON } from './models/VirtualMachineListDto';
import { VirtualMachineDto } from './models/VirtualMachineDto';
import axios from 'axios';
import { VCPUUnitsDto } from './models/VCPUUnitsDto';
import { MemoryUnitsDto } from './models/MemoryUnitsDto';

function vm(id: number): VirtualMachineDto {
    return {
        id: `vm-${id}`,
        meta: {
            'env': 'test',
        },
        poolId: 'client_vm',
        ram: {
            amount: 32.0,
            unit: MemoryUnitsDto.Gb,
        },
        storage: {
            amount: 1.0,
            unit: MemoryUnitsDto.Tb,
        },
        vcpu: {
            amount: 8,
            unit: VCPUUnitsDto.VCpu,
        }
    } as VirtualMachineDto
}

function vms(n: number): VirtualMachineListDto {
    return {
        period: {
            amount: 1.0,
            unit: 'hour',
        },
        virtualMachines: [...Array(n).keys()].map(idx => vm(idx)),
    }
}

describe('vm load testing', () => {
    it('run', (done) => {
        // given
        const payload = vms(1000)
        const options = {
            method: 'POST',
            url: 'http://localhost:8080/virtual_machines/assess',
            headers: {
                'Content-Type': 'application/json',
            },
            data: VirtualMachineListDtoToJSON(payload),
        }

        // when
        axios.request(options).then( (resp) => {
            const { data } = resp
            console.log(JSON.stringify(data, null, 4))
            done()
        }).catch((err) => {
            console.log(err)
            fail(err)
        })
    }, 60000)
})
