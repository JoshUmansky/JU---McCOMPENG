#include <iostream>
#include <vector>

#include "../header/LSQ.h"
#include "../header/MemTemplate.h"
#include "../header/CpuCoreGenerator.h"

namespace ns3 {

    // override ns3 type
    TypeId LSQ::GetTypeId(void) {
        static TypeId tid = TypeId("ns3::LSQ")
               .SetParent<Object > ();
        return tid;
    }

    // The only constructor
    LSQ::LSQ() {
        // default
        MAX_ENTRIES = 8;
        num_entries = 0;
        lsq_q = std::vector<CpuFIFO::ReqMsg>(MAX_ENTRIES);
    }

    // We don't do any dynamic allocations
    LSQ::~LSQ() {
    }

    void LSQ::init() {
        //do we need something here?


    }

    void LSQ::setMaxEntries(int max) {
        MAX_ENTRIES = max;
    }
    void LSQ::setNumEntries(int num) {
        num_entries = num;
    }
    int LSQ::getNumEntries() {
        return num_entries;
    }
    int LSQ::getMaxEntries() {
        return MAX_ENTRIES;
    }

    uint64_t LSQ::getID(){
        return lsq_q[0].msgId;
    }

    void LSQ::step(){
        //push to the existing TX and recieve from the Rx functions

        //retire any ready instructions
        for(int i = 0; i < num_entries; i++){
            if(lsq_q[i].ready == 1){
                retire(lsq_q[i]);
            }
        }

    }

    bool LSQ::canAccept(){
        //return if there is space in the LSQ
        return num_entries < MAX_ENTRIES; 
    }

    void LSQ::allocate(CpuFIFO::ReqMsg inst)
    {
        //allocate a new entry in the LSQ
        if(inst.type == CpuFIFO::WRITE){
            inst.ready = 1;
        }
        lsq_q[num_entries] = inst;
        num_entries++;
    }
    void LSQ::retire(CpuFIFO::ReqMsg inst) {
        //retire the provided instruction
        for(int i = 0; i < num_entries; i++){
            if(lsq_q[i].msgId == inst.msgId){
                for(int j = i; j < num_entries - 1; j++){
                    lsq_q[j] = lsq_q[j+1];
                }
            }
        }
        
        num_entries--;        
    }

    void LSQ::ldFwd(CpuFIFO::ReqMsg InstLook, CpuFIFO::ReqMsg InstFound) {
        // Forward from an existing entry in the LSQ if address matches
        // Logic handled in the memory controller
        for(int i = 0; i < InstLook.data.size(); i++) {  // Now works with std::array
            InstLook.data[i] = InstFound.data[i];
        }
        InstLook.ready = 1;
    }

    void LSQ::commit(CpuFIFO::ReqMsg Inst){
        Inst.ready = 1;
    }

    void LSQ::pushToCache(CpuCoreGenerator* cpuCoreController){
        //push to the cache
        cpuCoreController->GetCpuFIFO()->m_txFIFO.InsertElement(lsq_q[0]);
        //cpuCoreController.m_cpuFIFO->m_txFIFO.PushElement(lsq_q[0]);    
    }
    void LSQ::rxFromCache(CpuFIFO::RespMsg ResponseMsg){
        //recieve from the cache
        //check if the response is for the oldest instruction in the lsq
        
        if(lsq_q[0].msgId == ResponseMsg.msgId){
            //forward the response to the cpu
            lsq_q[0].ready = 1;
            
        }
    }

}

