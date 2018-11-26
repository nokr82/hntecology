package hntecology.ecology.model

import java.io.Serializable

class Birds (
        var no: Int? , var taxon: String? , var zoological: String? ,var name_kr: String? , var author: String? , var year: String?, var Phylum_name: String?, var Phylum_name_kr: String?, var Class_name: String? , var Class_name_kr: String?
        , var Order_name: String?, var Order_name_kr: String?, var Family_name: String?, var Family_name_kr: String?, var Genus_name: String?, var Genus_name_kr: String?, var Species_name: String?
        , var Species_name_kr: String?, var chkSelect: Boolean
): Serializable {
}
