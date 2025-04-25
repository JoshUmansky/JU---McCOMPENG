/*
 * File  :      MCoreSimProjectXml.h
 * Author:      Salah Hessien
 * Email :      salahga@mcmaster.ca
 *
 * Created On February 16, 2020
 */

#include "../header/CpuCoreGenerator.h"
#include "../header/LSQ.h"
#include "../header/ROB.h"

namespace ns3 {

    // override ns3 type
    TypeId CpuCoreGenerator::GetTypeId(void) {
        static TypeId tid = TypeId("ns3::CpuCoreGenerator")
               .SetParent<Object > ();
        return tid;
    }

    // The only constructor
    CpuCoreGenerator::CpuCoreGenerator(CpuFIFO* associatedCpuFIFO) {
        // default
        m_coreId         = 1;
        m_cpuCycle       = 1;
        m_bmFileName     = "trace_C0.trc";
        m_dt             = 1;
        m_clkSkew        = 0;
        m_cpuMemReq      = CpuFIFO::ReqMsg();
        m_cpuMemResp     = CpuFIFO::RespMsg();
        m_cpuFIFO        = associatedCpuFIFO;
        m_cpuReqDone     = false;
        m_newSampleRdy   = false;
        m_cpuCoreSimDone = false;
        m_logFileGenEnable = false;
        m_prevReqFinish    = true;
        m_prevReqFinishCycle = 0;
        m_prevReqArriveCycle = 0;
        m_cpuReqCnt      = 0;
        m_cpuRespCnt     = 0;
        m_number_of_OoO_requests = 1;
        stall_counter_compute = 0; //0 equal good, anything else is bad
        stall_counter_mem = 0;
    }

    // We don't do any dynamic allocations
    CpuCoreGenerator::~CpuCoreGenerator() {
    }

    // set Benchmark file name
    void CpuCoreGenerator::SetBmFileName (std::string bmFileName) {
        m_bmFileName = bmFileName;
    } 

    void CpuCoreGenerator::SetCpuTraceFile (std::string fileName) {
        m_cpuTraceFileName = fileName; 
    }

    void CpuCoreGenerator::SetCtrlsTraceFile (std::string fileName) {
        m_CtrlsTraceFileName = fileName;
    }

    // set CoreId
    void CpuCoreGenerator::SetCoreId (int coreId) {
      m_coreId = coreId;
    }

    // get core id
    int CpuCoreGenerator::GetCoreId () {
      return m_coreId;
    }

    // set dt
    void CpuCoreGenerator::SetDt (double dt) {
      m_dt = dt;
    }

    // get dt
    int CpuCoreGenerator::GetDt () {
      return m_dt;
    }

    // set clk skew
    void CpuCoreGenerator::SetClkSkew (double clkSkew) {
       m_clkSkew = clkSkew;
    }

    // get simulation done flag
    bool CpuCoreGenerator::GetCpuSimDoneFlag() {
      return m_cpuCoreSimDone;
    }

    void CpuCoreGenerator::SetLogFileGenEnable (bool logFileGenEnable ) {
      m_logFileGenEnable = logFileGenEnable;
    }

    void CpuCoreGenerator::SetOutOfOrderStages(int stages)
    {
      m_number_of_OoO_requests = stages;
      //std::cout<<"stages="<<m_number_of_OoO_requests<<std::endl;
    }
    //write function to get the pointer of the ifo
    CpuFIFO* CpuCoreGenerator::GetCpuFIFO() {
      return m_cpuFIFO;
    }
    
    // The init function starts the generator calling once very m_dt NanoSeconds.
    void CpuCoreGenerator::init() {
        m_bmTrace.open(m_bmFileName.c_str());
        Simulator::Schedule(NanoSeconds(m_clkSkew), &CpuCoreGenerator::Step, Ptr<CpuCoreGenerator>(this), Ptr<CpuCoreGenerator>(this));
    }

    // This function does most of the functionality.
    void CpuCoreGenerator::ProcessTxBuf() {
        //std::string fline;
        //uint64_t newArrivalCycle;
        
        Logger::getLogger()->setClkCount(this->m_coreId, this->m_cpuCycle);
        
        if (m_cpuFIFO->m_txFIFO.IsEmpty() == true)      
        {  
          //check if the front object in the lsq is already in the txFIFO
          m_lsq.pushToCache(this);
           //if(m_sent_requests < m_number_of_OoO_requests)
           //{
            //if (m_newSampleRdy == true) { // wait until reading from file
                //newArrivalCycle  = m_prevReqFinishCycle + m_cpuMemReq.cycle - m_prevReqArriveCycle;

                //if (m_cpuCycle >= newArrivalCycle) {
                  // reset all flag when new request inserted in the FIFO*****************
                  //m_newSampleRdy   = false;
                  //m_prevReqFinish  = false;
                  //This is the cycle when the request is inserted in the FIFO****************
                  //m_cpuMemReq.fifoInserionCycle = m_cpuCycle;

                  //m_cpuFIFO->m_txFIFO.InsertElement(m_cpuMemReq);
                  
                  //ending here
                  Logger::getLogger()->addRequest(this->m_coreId, m_cpuMemReq);
                  m_sent_requests++;
                  if (m_logFileGenEnable) {
                    std::cout << "Cpu " << m_coreId << " MemReq: ReqId = " << m_cpuMemReq.msgId << ", CpuRefCycle = " 
                              << m_cpuMemReq.cycle << ", CpuClkTic ==================================================== " <<  m_cpuCycle << std::endl;
                    std::cout << "\t\tMemAddr = " << m_cpuMemReq.addr << ", ReqType (0:Read, 1:Write) = " << m_cpuMemReq.type << ", CpuTxFIFO Size = " << m_cpuFIFO->m_txFIFO.GetQueueSize() << std::endl << std::endl;
                  }
                //}
            //}
           //}
            //written in step function for now with the address/changes needed I think
            /*
          if (m_newSampleRdy == false) {
            
            //Original Version
            //56036943c2a4 1 R 100
            //Address, Shared, Type, Cycle

            
            //New version 
            //20 47913324784448 W
            //number of compute instruction, address, type

            //this needs to be called every step, and then put into the q's*******************
            if (getline(m_bmTrace,fline)) {
             m_newSampleRdy    = true;
             size_t pos        = fline.find(" ");
             std::string s     = fline.substr(0, pos); 
             std::string dummy = fline.substr(pos+1, 1); 
             std::string type  = fline.substr(pos+3, 1);
             std::string cyc   = fline.substr(pos+5);

             // convert hex string address to decimal 
             m_cpuMemReq.addr = (uint64_t) strtol(s.c_str(), NULL, 16);

             // convert cycle from string to decimal, same value as the file
             m_cpuMemReq.cycle= (uint64_t) strtol(cyc.c_str(), NULL, 10);

             m_cpuMemReq.type = (type == "R") ? CpuFIFO::REQTYPE::READ : CpuFIFO::REQTYPE::WRITE;
             //m_cpuMemReq.type = CpuFIFO::REQTYPE::WRITE;
             Ptr<UniformRandomVariable> uRnd1;
             uRnd1 = CreateObject<UniformRandomVariable> ();
             uRnd1-> SetAttribute ("Min", DoubleValue (0));
             uRnd1-> SetAttribute ("Max", DoubleValue (100));
             //m_cpuMemReq.type = (uRnd1->GetValue() <= 50) ? CpuFIFO::REQTYPE::READ : CpuFIFO::REQTYPE::WRITE;

             // Generate unique Id for every cpu request (needed to avoid any 
             // collisions with other cores and to make debugging easier).
             m_cpuMemReq.msgId     = IdGenerator::nextReqId();
             m_cpuMemReq.reqCoreId = m_coreId;
             m_cpuReqCnt++;
            }
          } 
        }

        if (m_bmTrace.eof()) {
          m_bmTrace.close();
          m_cpuReqDone = true;
        }
                   */
        }
    } // void CpuCoreGenerator::ProcessTxBuf()
        void CpuCoreGenerator::ProcessRxBuf() {
        // process received buffer
        if (!m_cpuFIFO->m_rxFIFO.IsEmpty()) {
          m_cpuMemResp = m_cpuFIFO->m_rxFIFO.GetFrontElement();
          m_cpuFIFO->m_rxFIFO.PopElement();

          m_lsq.rxFromCache(m_cpuMemResp);
          m_rob.rxFromCache(m_cpuMemResp);

          Logger::getLogger()->updateRequest(m_cpuMemResp.msgId, Logger::EntryId::CPU_RX_CHECKPOINT);
          m_sent_requests--;
          if(m_sent_requests < 0)
            std::cout << "error" << std::endl;
          if (m_logFileGenEnable) {
            std::cout << "Cpu " << m_coreId << " new response is received at cycle " << m_cpuCycle << std::endl;
          }
          m_prevReqFinish      = true;
          m_prevReqFinishCycle = m_cpuCycle;
          m_prevReqArriveCycle = m_cpuMemResp.reqcycle;
          m_cpuRespCnt++;
        }
 
        // schedule next run or finish simulation if processing end
        if (m_cpuReqDone == true && m_cpuRespCnt >= m_cpuReqCnt) {
          m_cpuCoreSimDone = true;
          Logger::getLogger()->traceEnd(this->m_coreId);
          std::cout << "Cpu " << m_coreId << " Simulation End @ processor cycle # " << m_cpuCycle << std::endl;
        }
        else {
          // Schedule the next run
          Simulator::Schedule(NanoSeconds(m_dt), &CpuCoreGenerator::Step, Ptr<CpuCoreGenerator>(this), Ptr<CpuCoreGenerator>(this));
          m_cpuCycle++;
        }

    } // CpuCoreGenerator::ProcessRxBuf()

    /**
     * Runs one mobility Step for the given vehicle generator.
     * This function is called each interval dt
     */
    void CpuCoreGenerator::Step(Ptr<CpuCoreGenerator> cpuCoreGenerator) {
        cpuCoreGenerator->ProcessTxBuf();
        cpuCoreGenerator->ProcessRxBuf();
            //this needs to be called every step, and then put into the q's*******************
            std::string fline;
            uint64_t compute_instructions;
            if (getline(m_bmTrace,fline) && stall_counter_compute == 0 && stall_counter_mem == 0) {
             m_newSampleRdy    = true;
             size_t pos        = fline.find(" ");
             size_t pos2 = fline.rfind(" ");
             std::string compute_instructions_s = fline.substr(0, pos); //number of compute instructions
             std::string address = fline.substr(pos + 1, pos2 - pos - 1); //address
             std::string type = fline.substr(pos2 + 1, 1); //type

             // convert string address to decimal 
             m_cpuMemReq.addr = (uint64_t) strtol(address.c_str(), NULL, 10);

            compute_instructions = (uint64_t) strtol(compute_instructions_s.c_str(), NULL, 10);

            if(type == "R") m_cpuMemReq.type = CpuFIFO::READ;
            if(type == "W") m_cpuMemReq.type = CpuFIFO::WRITE;

            m_cpuMemReq.msgId     = IdGenerator::nextReqId();
            m_cpuMemReq.reqCoreId = m_coreId;
            m_cpuMemReq.addr = (uint64_t) strtol(address.c_str(), NULL, 10);
            m_cpuReqCnt++;
            }//end of new memreq

            //general step logic
            if(stall_counter_mem == 0){
              uint64_t i;
              if(stall_counter_compute != 0){
                i = stall_counter_compute;
              }
              else{
                i = 0;
              }
              for(i; i < compute_instructions; i++){
                //create a new memReq for each computer instrcution, and allocate to ROB
                CpuFIFO::ReqMsg newReq = m_cpuMemReq;
                newReq.msgId = IdGenerator::nextReqId();
                newReq.type = CpuFIFO::REQTYPE::COMPUTE;
                newReq.addr = 0;
                if(m_rob.canAccept()){
                  m_rob.allocate(newReq);
                }
                else{
                  stall_counter_compute = i;                
                  break;
                }
                if(i == compute_instructions - 1){
                  stall_counter_compute = 0;
                }
              }
            }
            
          if(stall_counter_compute == 0){
            if(m_cpuMemReq.type == CpuFIFO::REQTYPE::WRITE){
              //need to allocate to LSQ
              if(m_lsq.canAccept() && m_rob.canAccept()){
                m_lsq.allocate(m_cpuMemReq);
                m_rob.allocate(m_cpuMemReq);
                stall_counter_mem = 0;
              }
              else{
                stall_counter_mem == 1;
              }
            }//end of write

            if(m_cpuMemReq.type == CpuFIFO::REQTYPE::READ){
              //need to allocate to LSQ
              //first can LSQ to see if ldFWD is possible
              int loadFWD = 0;
              for(int i = m_lsq.getNumEntries() - 1; i >= 0; i--){
                if(m_lsq.lsq_q[i].addr == m_cpuMemReq.addr && m_lsq.lsq_q[i].type == CpuFIFO::REQTYPE::WRITE){
                  //need to forward the data
                  m_lsq.ldFwd(m_cpuMemReq, m_lsq.lsq_q[i]);
                  loadFWD = 1;
                  break;
                }
              }
              if(loadFWD == 1){
                if(m_rob.canAccept()){
                  m_rob.allocate(m_cpuMemReq);
                  stall_counter_mem = 0;
                }
                else{
                  stall_counter_mem == 1;
                }
              }
              else{
                if(m_lsq.canAccept() && m_rob.canAccept()){
                  m_lsq.allocate(m_cpuMemReq);
                  m_rob.allocate(m_cpuMemReq);
                  stall_counter_mem = 0;
                }
                else{
                  stall_counter_mem == 1;
                }
              }
            }//end of read

          }//end of memory stalling logic

        //ROB and LSQ steps
        m_rob.step();
        m_lsq.step();
        //if trace file is done, end the simulation
        if (m_bmTrace.eof()) {
          m_bmTrace.close();
          m_cpuReqDone = true;
        } 
        
      }//end of step function
}
