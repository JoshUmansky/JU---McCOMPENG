#ifndef ROB_H
#define ROB_H

#include <vector>
#include "MemTemplate.h"
#include "ns3/object.h"

namespace ns3{

    class ROB : public ns3::Object
    {
    public:
        static TypeId GetTypeId(void);
        ROB();
        ~ROB();
        void init();
        void setMaxEntries(int max);
        void setNumEntries(int num);
        int getNumEntries();
        int getMaxEntries();
        void setIPC(int ipc);
        int getIPC();
        void step();
        bool canAccept();
        void allocate(CpuFIFO::ReqMsg inst);
        void retire();
        void commit(CpuFIFO::ReqMsg);
        void rxFromCache(CpuFIFO::RespMsg ResponseMsg);
    private:
        int MAX_ENTRIES;
        int num_entries;
        int IPC;
        std::vector<CpuFIFO::ReqMsg> rob_q;
    };
}
#endif // ROB_H