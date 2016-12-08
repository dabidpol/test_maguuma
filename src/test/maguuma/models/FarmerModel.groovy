package test.maguuma.models;

import com.rameses.rcp.annotations.*;
import com.rameses.rcp.common.*;
import com.rameses.seti2.models.*;
import com.rameses.osiris2.client.*
import com.rameses.osiris2.common.*;
import com.rameses.util.*;

class FarmerModel extends CrudFormModel{
    @Binding
    def binding;
    
    @Service('DateService')
    def dtSvc
    
    @Service('SequenceService')
    def seqSvc
    
    @Service("PersistenceService")
    def persistenceSvc;
    
//    @Service("FarmerApproveService")
//    def fasSvc

    String serviceName = 'FarmerService';
    String entityName = 'farmer';
    
    
    //boolean editAllowed = false;
    
    boolean isAllowApprove() {
         return ( mode=='read' && entity.state.toString().matches('DRAFT|ACTIVE') ); 
    }
    
    def tenurials = ['OWNED', 'RENTED', 'LEASED', 'TENANT', 'OTHERS'];
    
    def units = ['AREA', 'HEADS', 'HILLS'];
    
    def selectedFarmerItem;
     
    public void afterCreate(){
        
        entity.farmerItems = [];
        entity.facilityItems = [];
        farmerItemHandler.reload();
        facilityItemHandler.reload();
        entity.farmerid = OsirisContext.env.ORGID + "-FARM" + seqSvc.getNextFormattedSeries('farmer');
            
    }
//    void test(o){
//        println o
//    }
        public void beforeSave(o){
//            if(!entity.items)throw new Exception("Items must not be empty")
            //println entity
            entity.dtcreated = dtSvc.getServerDate();
            entity.createdby_name = OsirisContext.env.FULLNAME;
            entity.createdby_id = OsirisContext.env.USERID;
            entity.state = "DRAFT";
            
            //entity.transmittalnum = dtSvc.getServerYear() +"-"+ dtSvc.getServerMonth() + seqSvc.getNextFormattedSeries('check' + dtSvc.getServerYear() + dtSvc.getServerMonth()) ;
            
        }
 
    def farmerItemHandler = [
        fetchList: { o->
            def p = [_schemaname: 'test_pagrifarmeritems'];
            p.findBy = [ 'parentid': entity.objid];
            p.select = "objid,parentid,address_text,commodity_objid,commodity_name,commoditytype_objid,commoditytype_name,commoditysubtype_objid,commoditysubtype_name,tenurialstatus,unit,qty,maintainer,remarks";
            if(!entity.farmerItems){
                entity.farmerItems = queryService.getList( p );
            }
            return entity.farmerItems;
        },
        createItem : {
           return[
               objid : 'FI' + new java.rmi.server.UID(),
               newitem : false,
           ]
        },
        onAddItem : {
            entity.farmerItems << it;
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')) {
                //service.deleteFarmerItems(it)
                entity.farmerItems.remove(it)
                farmerItemHandler.reload();
                return true;
                }
            return false;
            }
            
        
    ] as EditorListModel;
    
    
    
        def selectedFacilityItem;
        
        def facilityItemHandler = [
            fetchList: { o->
                def p = [_schemaname: 'test_pagrifarmerphf'];
                p.findBy = [ 'parentid': entity.objid];
                p.select = "objid,parentid,phf_objid,phf_name,phfnumber";
                if(!entity.facilityItems){
                    entity.facilityItems = queryService.getList( p );
                }
                return entity.facilityItems;
                },
                createItem : {
                    return[
                        objid : 'FF' + new java.rmi.server.UID()
                    ]
                 },
                onAddItem : {
                    entity.facilityItems << it;
                },
                onRemoveItem : {
                    if (MsgBox.confirm('Delete item?')){                
                        entity.facilityItems.remove(it);
                        facilityItemHandler.reload();
                        return true;
                    }
                    return false;
                }              
                         
        ] as EditorListModel;
    
    //    
//    ========== Lookup Location =========
    def getLookupAddress(){
        if(!selectedFarmerItem.address?.objid) {
            def h = { o->
                selectedFarmerItem.address = o;
            };
            def m = selectedFarmerItem.address;
            if(!m) m = [:];
            return Inv.lookupOpener( "address:editor", [handler:h, entity:m] );
        }
        else {
            def h = { o->
                o._schemaname = "entity_address";
                persistenceSvc.update( o );
                selectedFarmerItem.address = o;
            };
            def m = persistenceSvc.read( [_schemaname:'entity_address', objid:selectedFarmerItem.address.objid] );
            return Inv.lookupOpener( "address:editor", [handler:h, entity:m] );
            //binding.refresh();
           
        }
        
    }
    //    ========== Lookup Commodity =========
    def getLookupCommodity(){
        return InvokerUtil.lookupOpener('pagricommodity:lookup');
    }
   
//         ========== Lookup type =========
        def getLookupPagritype(){
            return Inv.lookupOpener('test_pagricommodity_type:lookup', [parentid:selectedFarmerItem.commodity.objid])
        }

    
    //         ========== Lookup Subtype =========
        def getLookupPagrisubtype(){
            return Inv.lookupOpener('test_pagricommodity_subtype:lookup', [parentid:selectedFarmerItem.commoditytype.objid])
        }
//       
//       ========== Lookup Facility ========= 
        def getLookupPagriphf(){
            return Inv.lookupOpener('phf:lookup')
        }
        
    
//    void changestate(){
//        switch(entity.state) {
//        case 'DRAFT':
//            entity.state = 'APPROVED';
//            break;
//            
////        case 'FORREVIEW':
////            entity.state = 'FORAPPROVAL';
////            break;
////            
////        case 'FORAPPROVAL':
////            entity.state = 'APPROVE';
////            break;
//        default:
//            break;
//        }
//        fasSvc.changestate(entity)
//   
//    }

    void approve() { 
        if ( MsgBox.confirm('You are about to approve this information. Proceed?')) { 
            getPersistenceService().update([ 
               _schemaname: 'test_pagrifarmer', 
               objid : entity.objid, 
               state : 'APPROVED' 
            ]); 
            loadData(); 
        }
    }
    
    

    }