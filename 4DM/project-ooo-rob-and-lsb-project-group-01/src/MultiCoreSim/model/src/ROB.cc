#include <iostream>
#include <vector>

#include "../header/ROB.h"
#include "../header/MemTemplate.h"

namespace ns3 {

    // override ns3 type
    TypeId ROB::GetTypeId(void) {
        static TypeId tid = TypeId("ns3::ROB")
               .SetParent<Object > ();
        return tid;
    }

    // The only constructor
    ROB::ROB() {
        // default
        MAX_ENTRIES = 32;
        num_entries = 0;
        IPC = 8;
        rob_q = std::vector<CpuFIFO::ReqMsg>(MAX_ENTRIES);
    }
    // We don't do any dynamic allocations
    ROB::~ROB() {
    }

    void ROB::init() {
        //do we need something here?


    }

    void ROB::setMaxEntries(int max) {
        MAX_ENTRIES = max;
    }
    void ROB::setNumEntries(int num) {
        num_entries = num;
    }
    int ROB::getNumEntries() {
        return num_entries;
    }
    int ROB::getMaxEntries() {
        return MAX_ENTRIES;
    }
    void ROB::setIPC(int ipc) {
        IPC = ipc;
    }
    int ROB::getIPC() {
        return IPC;
    }
    void ROB::rxFromCache(CpuFIFO::RespMsg ResponseMsg){
        //recieve from the cache
        for(int i = 0; i < num_entries; i++){
            if(rob_q[i].msgId == ResponseMsg.msgId){
                rob_q[i].ready = 1;
            }
        }
    }

    void ROB::step(){
        //loop through the ROB and check if the oldest entry is ready to retire, keep going until it isnt
        int retired_instructions = 0;
        for(int i = 0; i < num_entries; i++){
            if(rob_q[i].ready == 1 && retired_instructions < IPC){
                retire();
                retired_instructions++;
            }
            else break;
        }
    }
    bool ROB::canAccept(){
        //return if there is space in the ROB
        return num_entries < MAX_ENTRIES; 
    }
    void ROB::allocate(CpuFIFO::ReqMsg Inst){
        //allocate a new entry in the ROB
        if(Inst.type == CpuFIFO::COMPUTE || Inst.type == CpuFIFO::WRITE){
            Inst.ready = 1;
        }
        rob_q[num_entries] = Inst;
        num_entries++;
    }
    void ROB::retire() {
        if (!rob_q.empty()) {
            rob_q.erase(rob_q.begin());
            num_entries--;
        }
    }

    void ROB::commit(CpuFIFO::ReqMsg Inst){
        Inst.ready = 1;
    }
}