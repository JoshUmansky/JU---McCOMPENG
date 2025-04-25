#ifndef LSQ_H
#define LSQ_H

#include <vector>
#include "MemTemplate.h"
#include "ns3/object.h"

namespace ns3{
    class CpuCoreGenerator; // forward declare

    class LSQ : public ns3::Object
    {
    public:
        static TypeId GetTypeId(void);
        LSQ();
        ~LSQ();
        void init();
        void setMaxEntries(int max);
        void setNumEntries(int num);
        int getNumEntries();
        int getMaxEntries();
        void step();
        bool canAccept();
        void allocate(CpuFIFO::ReqMsg inst);
        void retire(CpuFIFO::ReqMsg inst);
        void ldFwd(CpuFIFO::ReqMsg InstLook, CpuFIFO::ReqMsg InstFound);
        void commit(CpuFIFO::ReqMsg);
        void pushToCache(CpuCoreGenerator* cpuCoreController);
        void rxFromCache(CpuFIFO::RespMsg ResponseMsg);
        std::vector<CpuFIFO::ReqMsg> lsq_q;
        uint64_t getID();
    private:
        int MAX_ENTRIES;
        int num_entries;
        
    };
}
#endif // LSQ_H
