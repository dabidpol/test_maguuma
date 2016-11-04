package test.maguuma.models;

import com.rameses.rcp.annotations.*;
import com.rameses.rcp.common.*;
import com.rameses.seti2.models.*;
import com.rameses.osiris2.client.*
import com.rameses.osiris2.common.*;
import com.rameses.util.*;

class FarmerTypeModel extends CrudFormModel{
    
    def getLookupCommodity(){
            return Inv.lookupOpener('pagricommodity:lookup',[
               onselect :{
                   entity.parentid = it.objid;
                   entity.parentname = it.name;
                   binding.refresh(); 
               },
           ])
        }


   
}